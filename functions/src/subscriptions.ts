import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import Stripe from 'stripe';

const stripe = new Stripe(functions.config().stripe.secret_key, {
    apiVersion: '2023-10-16'
});

const db = admin.firestore();

export const createPaymentIntent = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { subscriptionType } = data;
    const userId = context.auth.uid;

    try {
        const amount = subscriptionType === 'monthly' ? 999 : 9800; // £9.99 or £98.00
        const customer = await getOrCreateCustomer(userId);

        const paymentIntent = await stripe.paymentIntents.create({
            amount,
            currency: 'gbp',
            customer: customer.id,
            automatic_payment_methods: {
                enabled: true,
            },
            metadata: {
                userId,
                subscriptionType
            }
        });

        return {
            clientSecret: paymentIntent.client_secret
        };
    } catch (error) {
        console.error('Error creating payment intent:', error);
        throw new functions.https.HttpsError('internal', 'Unable to create payment intent');
    }
});

export const createSubscription = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { paymentMethodId, planId } = data;
    const userId = context.auth.uid;

    try {
        const customer = await getOrCreateCustomer(userId);
        
        // Attach payment method to customer
        await stripe.paymentMethods.attach(paymentMethodId, {
            customer: customer.id,
        });

        // Set as default payment method
        await stripe.customers.update(customer.id, {
            invoice_settings: {
                default_payment_method: paymentMethodId,
            },
        });

        // Create subscription
        const priceId = getPriceId(planId);
        const subscription = await stripe.subscriptions.create({
            customer: customer.id,
            items: [{ price: priceId }],
            payment_behavior: 'default_incomplete',
            payment_settings: { save_default_payment_method: 'on_subscription' },
            expand: ['latest_invoice.payment_intent'],
            metadata: {
                userId,
                planId
            }
        });

        return {
            subscriptionId: subscription.id,
            clientSecret: (subscription.latest_invoice as Stripe.Invoice)
                .payment_intent?.client_secret
        };
    } catch (error) {
        console.error('Error creating subscription:', error);
        throw new functions.https.HttpsError('internal', 'Unable to create subscription');
    }
});

export const cancelSubscription = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { subscriptionId } = data;
    const userId = context.auth.uid;

    try {
        const subscription = await stripe.subscriptions.cancel(subscriptionId);
        
        // Update Firestore
        await db.collection('users')
            .doc(userId)
            .collection('subscription')
            .doc('status')
            .update({
                isActive: false,
                endDate: admin.firestore.Timestamp.fromDate(
                    new Date(subscription.current_period_end * 1000)
                ),
                isGracePeriod: true,
                gracePeriodEndDate: admin.firestore.Timestamp.fromDate(
                    new Date(Date.now() + 30 * 24 * 60 * 60 * 1000) // 30 days
                )
            });

        return { success: true };
    } catch (error) {
        console.error('Error canceling subscription:', error);
        throw new functions.https.HttpsError('internal', 'Unable to cancel subscription');
    }
});

// Stripe webhook handler for subscription events
export const handleStripeWebhook = functions.https.onRequest(async (req, res) => {
    const sig = req.headers['stripe-signature'];
    const webhookSecret = functions.config().stripe.webhook_secret;

    try {
        const event = stripe.webhooks.constructEvent(
            req.rawBody,
            sig as string,
            webhookSecret
        );

        switch (event.type) {
            case 'customer.subscription.created':
            case 'customer.subscription.updated':
                await handleSubscriptionUpdated(event.data.object as Stripe.Subscription);
                break;
            case 'customer.subscription.deleted':
                await handleSubscriptionDeleted(event.data.object as Stripe.Subscription);
                break;
            case 'invoice.payment_failed':
                await handlePaymentFailed(event.data.object as Stripe.Invoice);
                break;
        }

        res.json({ received: true });
    } catch (error) {
        console.error('Error handling webhook:', error);
        res.status(400).send('Webhook Error');
    }
});

// Helper functions
async function getOrCreateCustomer(userId: string): Promise<Stripe.Customer> {
    const userSnapshot = await db.collection('users').doc(userId).get();
    const userData = userSnapshot.data();

    if (userData?.stripeCustomerId) {
        return await stripe.customers.retrieve(userData.stripeCustomerId) as Stripe.Customer;
    }

    const customer = await stripe.customers.create({
        metadata: {
            userId
        }
    });

    await db.collection('users').doc(userId).update({
        stripeCustomerId: customer.id
    });

    return customer;
}

function getPriceId(planId: string): string {
    switch (planId) {
        case 'monthly':
            return 'price_monthly'; // Replace with your actual Stripe price ID
        case 'annual':
            return 'price_annual'; // Replace with your actual Stripe price ID
        default:
            throw new Error('Invalid plan ID');
    }
}

async function handleSubscriptionUpdated(subscription: Stripe.Subscription) {
    const userId = subscription.metadata.userId;
    if (!userId) return;

    await db.collection('users')
        .doc(userId)
        .collection('subscription')
        .doc('status')
        .set({
            subscriptionId: subscription.id,
            status: subscription.status,
            currentPeriodEnd: admin.firestore.Timestamp.fromDate(
                new Date(subscription.current_period_end * 1000)
            ),
            isActive: subscription.status === 'active',
            cancelAtPeriodEnd: subscription.cancel_at_period_end
        }, { merge: true });
}

async function handleSubscriptionDeleted(subscription: Stripe.Subscription) {
    const userId = subscription.metadata.userId;
    if (!userId) return;

    await db.collection('users')
        .doc(userId)
        .collection('subscription')
        .doc('status')
        .update({
            isActive: false,
            status: 'canceled',
            endDate: admin.firestore.Timestamp.fromDate(
                new Date(subscription.ended_at ? subscription.ended_at * 1000 : Date.now())
            )
        });
}

async function handlePaymentFailed(invoice: Stripe.Invoice) {
    const subscription = await stripe.subscriptions.retrieve(invoice.subscription as string);
    const userId = subscription.metadata.userId;
    if (!userId) return;

    await db.collection('users')
        .doc(userId)
        .collection('subscription')
        .doc('status')
        .update({
            isActive: false,
            status: 'payment_failed',
            lastPaymentError: invoice.last_payment_error?.message
        });
} 