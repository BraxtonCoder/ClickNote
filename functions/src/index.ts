import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";
import Stripe from "stripe";

admin.initializeApp();

const stripeConfig = { apiVersion: "2025-01-27.acacia" };
const stripeClient = new Stripe(functions.config().stripe.secret_key, stripeConfig);

interface SubscriptionPrices {
  [key: string]: {
    priceId: string;
    amount: number;
  };
}

const SUBSCRIPTION_PRICES: SubscriptionPrices = {
  monthly: {
    priceId: "price_monthly",
    amount: 999,
  },
  annual: {
    priceId: "price_annual",
    amount: 9800,
  }
};

interface CreatePaymentIntentData {
  subscriptionType: string;
}

export const createPaymentIntent = functions.https.onCall(async (data: any, context) => {
  if (!context?.auth?.uid) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "You must be logged in to create a payment intent"
    );
  }

  try {
    const { subscriptionType } = data as CreatePaymentIntentData;
    const priceData = SUBSCRIPTION_PRICES[subscriptionType];

    if (!priceData) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Invalid subscription type"
      );
    }

    const paymentIntent = await stripeClient.paymentIntents.create({
      amount: priceData.amount,
      currency: "gbp",
      metadata: {
        userId: context.auth.uid,
        subscriptionType,
      }
    });

    return { clientSecret: paymentIntent.client_secret };
  } catch (error) {
    console.error("Error creating payment intent:", error);
    throw new functions.https.HttpsError(
      "internal",
      "An error occurred while creating the payment intent"
    );
  }
});

interface CreateSubscriptionData {
  subscriptionType: string;
}

export const createSubscription = functions.https.onCall(async (data: any, context) => {
  if (!context?.auth?.uid) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "You must be logged in to create a subscription"
    );
  }

  try {
    const { subscriptionType } = data as CreateSubscriptionData;
    const priceData = SUBSCRIPTION_PRICES[subscriptionType];

    if (!priceData) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Invalid subscription type"
      );
    }

    // Get or create Stripe customer
    const userDoc = await admin.firestore().collection("users").doc(context.auth.uid).get();
    let stripeCustomerId = userDoc.data()?.stripeCustomerId;

    if (!stripeCustomerId) {
      const customer = await stripeClient.customers.create({
        metadata: {
          userId: context.auth.uid,
        }
      });
      stripeCustomerId = customer.id;

      // Save Stripe customer ID to Firestore
      await admin.firestore().collection("users").doc(context.auth.uid).update({
        stripeCustomerId: customer.id,
      });
    }

    // Create subscription
    const subscription = await stripeClient.subscriptions.create({
      customer: stripeCustomerId,
      items: [{ price: priceData.priceId }],
      payment_behavior: "default_incomplete",
      expand: ["latest_invoice.payment_intent"],
    });

    // Update user's subscription status in Firestore
    await admin.firestore().collection("users").doc(context.auth.uid).update({
      subscriptionId: subscription.id,
      subscriptionStatus: subscription.status,
      subscriptionType,
    });

    const invoice = subscription.latest_invoice as Stripe.Invoice;
    const paymentIntent = invoice.payment_intent as Stripe.PaymentIntent;

    return {
      subscriptionId: subscription.id,
      clientSecret: paymentIntent.client_secret,
    };
  } catch (error) {
    console.error("Error creating subscription:", error);
    throw new functions.https.HttpsError(
      "internal",
      "An error occurred while creating the subscription"
    );
  }
});

export const handleWebhook = functions.https.onRequest(async (req, res) => {
  const sig = req.headers["stripe-signature"];

  try {
    const event = stripeClient.webhooks.constructEvent(
      req.rawBody,
      sig as string,
      functions.config().stripe.webhook_secret
    );

    switch (event.type) {
    case "payment_intent.succeeded":
      await handlePaymentIntentSucceeded(event.data.object as Stripe.PaymentIntent);
      break;
    case "payment_intent.payment_failed":
      await handlePaymentIntentFailed(event.data.object as Stripe.PaymentIntent);
      break;
    }

    res.json({ received: true });
  } catch (error) {
    console.error("Webhook error:", error);
    res.status(400).send("Webhook Error");
  }
});

async function handlePaymentIntentSucceeded(paymentIntent: Stripe.PaymentIntent) {
  const { userId } = paymentIntent.metadata;
  await admin.firestore().collection("users").doc(userId).update({
    paymentStatus: "succeeded",
  });
}

async function handlePaymentIntentFailed(paymentIntent: Stripe.PaymentIntent) {
  const { userId } = paymentIntent.metadata;
  await admin.firestore().collection("users").doc(userId).update({
    paymentStatus: "failed",
  });
}

// Get subscription status
export const getSubscriptionStatus = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "User must be logged in");
  }

  try {
    const userDoc = await admin.firestore().collection("users").doc(context.auth.uid).get();
    const userData = userDoc.data();

    if (!userData?.subscriptionId) {
      return { status: "none" };
    }

    const subscription = await stripeClient.subscriptions.retrieve(userData.subscriptionId);
    return {
      status: subscription.status,
      periodEnd: subscription.current_period_end,
      priceId: subscription.items.data[0].price.id,
    };
  } catch (error) {
    console.error("Error getting subscription status:", error);
    throw new functions.https.HttpsError("internal", "Unable to get subscription status");
  }
});

export const updateSubscriptionStatus = functions.https.onRequest(async (req, res) => {
  const event = req.body;

  if (event.type === "customer.subscription.updated") {
    const subscription = event.data.object;
    const customerId = subscription.customer;

    // Get user by Stripe customer ID
    const userSnapshot = await admin.firestore()
      .collection("users")
      .where("stripeCustomerId", "==", customerId)
      .get();

    if (!userSnapshot.empty) {
      const userDoc = userSnapshot.docs[0];
      await userDoc.ref.update({
        subscriptionStatus: subscription.status,
      });
    }
  }

  res.json({ received: true });
});

export const cancelSubscription = functions.https.onCall(async (data: any, context) => {
  if (!context?.auth?.uid) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "You must be logged in to cancel a subscription"
    );
  }

  try {
    const userDoc = await admin.firestore().collection("users").doc(context.auth.uid).get();
    const { subscriptionId } = userDoc.data() || {};

    if (!subscriptionId) {
      throw new functions.https.HttpsError(
        "not-found",
        "No active subscription found"
      );
    }

    await stripeClient.subscriptions.cancel(subscriptionId);

    await admin.firestore().collection("users").doc(context.auth.uid).update({
      subscriptionStatus: "canceled",
    });

    return { success: true };
  } catch (error) {
    console.error("Error canceling subscription:", error);
    throw new functions.https.HttpsError(
      "internal",
      "An error occurred while canceling the subscription"
    );
  }
});
