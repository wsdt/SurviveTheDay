package kevkevin.wsdt.tagueberstehen.classes.manager.interfaces;

public interface IConstants_InAppPurchaseMgr {
    boolean USE_STATIC_TEST_INAPP_PRODUCTS = false;

    interface BASE64ENCODED_PUBLICKEY {
        char SEPARATOR = '/';
        String[] substr_arr = new String[]{
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjgvR22VPMnYUNA6WRQIwrgnjLUp1hb+fkHB2nkyJSq9sg9LaY1fs",
                "0TLTPpRpDWHipYfI58671lIuUKL",
                "pOokXDC6RMefoRYhNS3ikl8O3",
                "sOkBB3vFJb+Fgwk+b",
                "FvIp3Xes33s",
                "UJ6ZiC1Eidt1fT3xbPWZd+ss76sp3remAzFcHJ44UeU9jVECowmfnMTuddv62lb1QSsPo3la4bmBIwTaGdsTyemx92wjwHUYkZdIBxHwIiRxM3as72Q6s1PIK+YLC7+kRzsxE0QYLcCGbMt1Y2Ox",
                "asv",
                "8yRbKLE63",
                "9RRpFjjRdyNc5HbtnKBoHVmr6TeKDGUJWgiomVExqQIDAQAB"};
    }

    interface TEST_INAPP_PRODUCTS {
        interface STATIC_TEST {
            //String BUY_PRODUCT_DEFAULT_RESPONSE = GOOGLE_PLAY_STATIC_RESPONSES.PRODUCT_CANCELED.toString(); //let test purchases success (here changeable)
            enum GOOGLE_PLAY_STATIC_RESPONSES { //as enum to iterate through it
                PRODUCT_PURCHASED("android.test.purchased"),
                PRODUCT_CANCELED("android.test.canceled"),
                PRODUCT_REFUND("android.test.refunded"),
                PRODUCT_ITEM_UNAVAILABLE("android.test.item_unavailable");

                private final String responseId;

                GOOGLE_PLAY_STATIC_RESPONSES(final String responseId) {
                    this.responseId = responseId;
                }

                @Override
                public String toString() {
                    return this.responseId;
                }
            }
        }
    }

    //As enum so we can iterate through it more easily and ensure data types
    enum INAPP_PRODUCTS { //all other informations should be delivered by Google Play (following infos not translateable)
        CHANGE_NOTIFICATION_COLOR("countdownnode_notificationcategory_color_change"), //implemented
        REMOVE_ALL_ADS("remove_ads"), //implemented
        USE_MORE_COUNTDOWN_NODES("use_more_countdownnodes"); //implemented

        private final String productId;

        INAPP_PRODUCTS(final String productId) {
            this.productId = productId;
        }

        @Override
        public String toString() {
            return this.productId;
        }
    }
}
