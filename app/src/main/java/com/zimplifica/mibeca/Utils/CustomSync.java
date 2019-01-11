package com.zimplifica.mibeca.Utils;

import com.amazonaws.GetDepositsByBeneficiaryQuery;
import com.amazonaws.GetSubscriptionsQuery;
import com.amazonaws.OnDeltaUpdateSubscription;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.AppSyncCallback;
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.internal.util.Cancelable;

import javax.annotation.Nonnull;

public class CustomSync {


    private AWSAppSyncClient client;

    public CustomSync(AWSAppSyncClient client){
        this.client = client;
    }

    public void sync (Query baseQuery, GraphQLCall.Callback baseQueryCallback, OnDeltaUpdateSubscription subscription, AppSyncSubscriptionCall.Callback subscriptionCallback,
                      Query deltaQuery, GraphQLCall.Callback deltaQueryCallback, long baseRefreshingIntervalInSeconds){

        Cancelable handle = client.sync(baseQuery, baseQueryCallback, subscription, subscriptionCallback, deltaQuery, deltaQueryCallback, baseRefreshingIntervalInSeconds);

    }

}
