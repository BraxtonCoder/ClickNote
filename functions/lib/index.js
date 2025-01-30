"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.cancelSubscription = exports.updateSubscriptionStatus = exports.getSubscriptionStatus = exports.handleWebhook = exports.createSubscription = exports.createPaymentIntent = void 0;
const functions = __importStar(require("firebase-functions/v1"));
const admin = __importStar(require("firebase-admin"));
const stripe_1 = __importDefault(require("stripe"));
admin.initializeApp();
const stripeClient = new stripe_1.default(functions.config().stripe.secret_key, { apiVersion: "2025-01-27.acacia" });
const SUBSCRIPTION_PRICES = {
    monthly: {
        priceId: "price_monthly",
        amount: 999,
    },
    annual: {
        priceId: "price_annual",
        amount: 9800,
    }
};
exports.createPaymentIntent = functions.https.onCall(async (data, context) => {
    var _a;
    if (!((_a = context === null || context === void 0 ? void 0 : context.auth) === null || _a === void 0 ? void 0 : _a.uid)) {
        throw new functions.https.HttpsError("unauthenticated", "You must be logged in to create a payment intent");
    }
    try {
        const { subscriptionType } = data;
        const priceData = SUBSCRIPTION_PRICES[subscriptionType];
        if (!priceData) {
            throw new functions.https.HttpsError("invalid-argument", "Invalid subscription type");
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
    }
    catch (error) {
        console.error("Error creating payment intent:", error);
        throw new functions.https.HttpsError("internal", "An error occurred while creating the payment intent");
    }
});
exports.createSubscription = functions.https.onCall(async (data, context) => {
    var _a, _b;
    if (!((_a = context === null || context === void 0 ? void 0 : context.auth) === null || _a === void 0 ? void 0 : _a.uid)) {
        throw new functions.https.HttpsError("unauthenticated", "You must be logged in to create a subscription");
    }
    try {
        const { subscriptionType } = data;
        const priceData = SUBSCRIPTION_PRICES[subscriptionType];
        if (!priceData) {
            throw new functions.https.HttpsError("invalid-argument", "Invalid subscription type");
        }
        // Get or create Stripe customer
        const userDoc = await admin.firestore().collection("users").doc(context.auth.uid).get();
        let stripeCustomerId = (_b = userDoc.data()) === null || _b === void 0 ? void 0 : _b.stripeCustomerId;
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
        const invoice = subscription.latest_invoice;
        const paymentIntent = invoice.payment_intent;
        return {
            subscriptionId: subscription.id,
            clientSecret: paymentIntent.client_secret,
        };
    }
    catch (error) {
        console.error("Error creating subscription:", error);
        throw new functions.https.HttpsError("internal", "An error occurred while creating the subscription");
    }
});
exports.handleWebhook = functions.https.onRequest(async (req, res) => {
    const sig = req.headers["stripe-signature"];
    try {
        const event = stripeClient.webhooks.constructEvent(req.rawBody, sig, functions.config().stripe.webhook_secret);
        switch (event.type) {
            case "payment_intent.succeeded":
                await handlePaymentIntentSucceeded(event.data.object);
                break;
            case "payment_intent.payment_failed":
                await handlePaymentIntentFailed(event.data.object);
                break;
        }
        res.json({ received: true });
    }
    catch (error) {
        console.error("Webhook error:", error);
        res.status(400).send("Webhook Error");
    }
});
async function handlePaymentIntentSucceeded(paymentIntent) {
    const { userId } = paymentIntent.metadata;
    await admin.firestore().collection("users").doc(userId).update({
        paymentStatus: "succeeded",
    });
}
async function handlePaymentIntentFailed(paymentIntent) {
    const { userId } = paymentIntent.metadata;
    await admin.firestore().collection("users").doc(userId).update({
        paymentStatus: "failed",
    });
}
// Get subscription status
exports.getSubscriptionStatus = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "User must be logged in");
    }
    try {
        const userDoc = await admin.firestore().collection("users").doc(context.auth.uid).get();
        const userData = userDoc.data();
        if (!(userData === null || userData === void 0 ? void 0 : userData.subscriptionId)) {
            return { status: "none" };
        }
        const subscription = await stripeClient.subscriptions.retrieve(userData.subscriptionId);
        return {
            status: subscription.status,
            periodEnd: subscription.current_period_end,
            priceId: subscription.items.data[0].price.id,
        };
    }
    catch (error) {
        console.error("Error getting subscription status:", error);
        throw new functions.https.HttpsError("internal", "Unable to get subscription status");
    }
});
exports.updateSubscriptionStatus = functions.https.onRequest(async (req, res) => {
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
exports.cancelSubscription = functions.https.onCall(async (data, context) => {
    var _a;
    if (!((_a = context === null || context === void 0 ? void 0 : context.auth) === null || _a === void 0 ? void 0 : _a.uid)) {
        throw new functions.https.HttpsError("unauthenticated", "You must be logged in to cancel a subscription");
    }
    try {
        const userDoc = await admin.firestore().collection("users").doc(context.auth.uid).get();
        const { subscriptionId } = userDoc.data() || {};
        if (!subscriptionId) {
            throw new functions.https.HttpsError("not-found", "No active subscription found");
        }
        await stripeClient.subscriptions.cancel(subscriptionId);
        await admin.firestore().collection("users").doc(context.auth.uid).update({
            subscriptionStatus: "canceled",
        });
        return { success: true };
    }
    catch (error) {
        console.error("Error canceling subscription:", error);
        throw new functions.https.HttpsError("internal", "An error occurred while canceling the subscription");
    }
});
//# sourceMappingURL=index.js.map