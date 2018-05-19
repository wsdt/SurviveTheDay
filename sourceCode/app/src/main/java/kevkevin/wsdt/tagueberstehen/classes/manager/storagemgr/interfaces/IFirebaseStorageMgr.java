package kevkevin.wsdt.tagueberstehen.classes.manager.storagemgr.interfaces;

public interface IFirebaseStorageMgr {
    /** Important: By changing the versionCode folder you should also UPDATE the upload-procedure Json of
     * the FirebaseStorageMgr.class, so the newly saved Userlibrary gets saved correctly for the specific version code! */
    String LIB_JSON_VERSION_FOLDER = "v1"; //versioning, so we can change the firebaseJson over time without any problems
    /** By changing this all index files and userLibraries need to have exact this file extension. */
    String RES_FILE_EXTENSION = "json";

    interface INDEX_FILES {
        String FILENAME = "__index"; //all index files on Firebase need to be named like this!
    }

    interface DEFAULT {
        /** Default user library (local copy) so the user does not need to have an internet for opening
         * the app the first time. Prevents errors and bad usability. */
        String LIB_JSON_DEFAULT = "{\n" +
                "\t\"libId\":\"0HASH00jkldsjdfs\", \t\t\t\t\t\n" +
                "\t\"libName\": \"Default quotes - English\",\t\t\n" +
                "\t\"libLanguageCode\": \"en\", \t\t\t\t\t\n" +
                "\t\"createdBy\": \"Kevin Riedl (WSDT)\",\t\t\t\n" +
                "\t\"createdOn\": 1524668400,\t\t\t\t\t\n" +
                "\t\"lastEditOn\": 1524668400,\t\t\t\t\t\n" +
                "\n" +
                "\t\"lines\": [\n" +
                "\t\t\"The way get started is to quit talking and begin doing. (Walt Disney)\",\n" +
                "\t\t\"The pessimist sees difficulty in every opportunity. The optimist sees opportunity in every difficulty. (Winston Churchill)\",\n" +
                "\t\t\"Don't let yesterday take up too much of today. (Will Rogers)\",\n" +
                "\t\t\"You learn more from failure than from success. Don't let it stop you. Failure build character.\",\n" +
                "\t\t\"It's not whether you get knocked down, it's whether you get up. (Vince Lombardi)\",\n" +
                "\t\t\"If you are working on something that you really care about, you don't have to be pushed. The vision pulls you. (Steve Jobs)\",\n" +
                "\t\t\"People who are crazy enough to think they can change the world, are the ones who do. (Rob Siltanen)\",\n" +
                "\t\t\"Failure will never overtake me if my determination to succeed is strong enough. (Og Mandino)\",\n" +
                "\t\t\"Entrepreneurs are great at dealing with uncertainty and also very good at minimizing risk. That's the classic entrepreneur. (Mohnish Pabrai)\",\n" +
                "\t\t\"We may encounter many defeats but we must not be defeated. (Maya Angelou)\",\n" +
                "\t\t\"Knowing is not enough; We must apply. Wishing is not enough; We must do. (Johann Wolfgang von Goethe)\",\n" +
                "\t\t\"Imagine your life is perfect in every respect; What would it look like? (Brian Tracy)\",\n" +
                "\t\t\"We generate fears while we sit. We overcome them by action. (Dr. Henry Link)\",\n" +
                "\t\t\"Whether you think you can or think you can't, you're right. (Henry Ford)\",\n" +
                "\t\t\"Security is mostly a superstition. Life is either a daring adventure or nothing. (Helen Keller)\",\n" +
                "\t\t\"The man who has confidence in himself gains the confidence of others. (Hasidic Proverb)\",\n" +
                "\t\t\"The only limit to our realization of tomorrow will be our doubts of today. (Franklin D. Roosevelt)\",\n" +
                "\t\t\"Creativity is intelligence having fun. (Albert Einstein)\",\n" +
                "\t\t\"What you lack in talent can be made up with desire, hustle and giving 110 % all the time. (Don Zimmer)\",\n" +
                "\t\t\"Do what you can with all you have, wherever you are. (Theodore Roosevelt)\",\n" +
                "\t\t\"Develop an attitude of gratitude. Say thank you to everyone you meet for everything they do for you. (Brian Tracy)\",\n" +
                "\t\t\"You are never too old to set another goal or to dream a new dream. (C.S. Lewis)\",\n" +
                "\t\t\"To see what is right and not do it is a lack of courage. (Confucious)\",\n" +
                "\t\t\"Reading is to the mind, as exercise to the body. (Brian Tracy)\",\n" +
                "\t\t\"Fake it until you make it! Act as if you had all the confidence you require until it becomes your reality. (Brian Tracy)\",\n" +
                "\t\t\"The future belong to the competent. Get good, get better, be the best! (Brian Tracy)\",\n" +
                "\t\t\"For every reason it's not possible, there are hundreds of people who have faced the same circumstances and succeeded. (Jack Canfield)\",\n" +
                "\t\t\"Things work out best for those who make the best of how things work out. (John Wooden)\",\n" +
                "\t\t\"A room without books is like a body without a soul. (Marcus Tullius Cicero)\",\n" +
                "\t\t\"I think goals should never be easy, they should force you to work, even if they are uncomfortable at the time. (Michael Phelps)\",\n" +
                "\t\t\"One of the lessons that I grew up with was to always stay true to yourself and never let what somebody else days distracts you from your goals. (Michelle Obama)\",\n" +
                "\t\t\"Today's accomplishments were yesterday's impossibilities. (Robert H. Schuller)\",\n" +
                "\t\t\"The only way to do great work is to love what you do. If you haven't found it yet, keep looking. Don't settle. (Steve Jobs)\",\n" +
                "\t\t\"You Don’t Have To Be Great To Start, But You Have To Start To Be Great. (Zig Ziglar)\",\n" +
                "\t\t\"A Clear Vision, Backed By Definite Plans, Gives You A Tremendous Feeling Of Confidence And Personal Power. (Brian Tracy)\",\n" +
                "\t\t\"There Are No Limits To What You Can Accomplish, Except The Limits You Place On Your Own Thinking. (Brian Tracy)\",\n" +
                "\t\t\"Integrity is the most valuable and respected quality of leadership. Always keep your word.\",\n" +
                "\t\t\"Leadership is the ability to get extraordinary achievement from ordinary people.\",\n" +
                "\t\t\"Leaders set high standards. Refuse to tolerate mediocrity or poor performance.\",\n" +
                "\t\t\"Clarity is the key to effective leadership. What are your goals?\",\n" +
                "\t\t\"The best leaders have a high Consideration Factor. They really care about their people.\",\n" +
                "\t\t\"Leaders think and talk about the solutions. Followers think and talk about the problems.\",\n" +
                "\t\t\"The key responsibility of leadership is to think about the future. No one else can do it for you.\",\n" +
                "\t\t\"The effective leader recognizes that they are more dependent on their people than they are on them. Walk softly.\",\n" +
                "\t\t\"Leaders never use the word failure. They look upon setbacks as learning experiences.\",\n" +
                "\t\t\"Practice Golden Rule Management in everything you do. Manage others the way you would like to be managed.\",\n" +
                "\t\t\"Superior leaders are willing to admit a mistake and cut their losses. Be willing to admit that you’ve changed your mind. Don’t persist when the original decision turns out to be a poor one.\",\n" +
                "\t\t\"Leaders are anticipatory thinkers. They consider all consequences of their behaviors before they act.\",\n" +
                "\t\t\"The true test of leadership is how well you function in a crisis.\",\n" +
                "\t\t\"Leaders concentrate single-mindedly on one thing– the most important thing, and they stay at it until it’s complete.\",\n" +
                "\t\t\"The three ‘C’s’ of leadership are Consideration, Caring, and Courtesy. Be polite to everyone.\",\n" +
                "\t\t\"Respect is the key determinant of high-performance leadership. How much people respect you determines how well they perform.\",\n" +
                "\t\t\"Leadership is more who you are than what you do.\",\n" +
                "\t\t\"Entrepreneurial leadership requires the ability to move quickly when opportunity presents itself.\",\n" +
                "\t\t\"Leaders are innovative, entrepreneurial, and future oriented. They focus on getting the job done.\",\n" +
                "\t\t\"Leaders are never satisfied; they continually strive to be better.\",\n" +
                "\t\t\"The best and most beautiful things in the world cannot be seen or even touched - they must be felt with the heart. (Helen Keller)\",\n" +
                "\t\t\"The best preparation for tomorrow is doing your best today. (H. Jackson Brown, Jr.)\",\n" +
                "\t\t\"I can't change the direction of the wind, but I can adjust my sails to always reach my destination. (Jimmy Dean)\",\n" +
                "\t\t\"We must let go of the life we have planned, so as to accept the one that is waiting for us. (Joseph Campbell)\",\n" +
                "\t\t\"You must do the things you think you cannot do. (Eleanor Roosevelt)\",\n" +
                "\t\t\"Put your heart, mind, and soul into even your smallest acts. This is the secret of success. (Swami Sivananda)\",\n" +
                "\t\t\"Start by doing what's necessary; then do what's possible; and suddenly you are doing the impossible. (Francis of Assisi)\",\n" +
                "\t\t\"The limits of the possible can only be defined by going beyond them into the impossible. (Arthur C. Clarke)\",\n" +
                "\t\t\"Happiness is not something you postpone for the future; it is something you design for the present. (Jim Rohn)\",\n" +
                "\t\t\"Try to be a rainbow in someone's cloud. (Maya Angelou)\",\n" +
                "\t\t\"It is during our darkest moments that we must focus to see the light. (Aristotle)\",\n" +
                "\t\t\"Health is the greatest gift, contentment the greatest wealth, faithfulness the best relationship. (Buddha)\",\n" +
                "\t\t\"Change your thoughts and you change the world. (Norman Vincent Peale)\",\n" +
                "\t\t\"Nothing is impossible, the word itself says 'I'm possible'. (Audrey Hepburn)\",\n" +
                "\t\t\"My mission in life is not merely to survive, but to thrive; and to do so with some passion, some compassion, some humor, and some style. (Maya Angelou)\",\n" +
                "\t\t\"Today I choose life. Every morning when I wake up I can choose joy, happiness, negativity, pain... To feel the freedom that comes from being able to continue to make mistakes and choices - today I choose to feel life, not to deny my humanity but embrace it. (Kevyn Aucoin)\",\n" +
                "\t\t\"Your work is going to fill a large part of your life, and the only way to be truly satisfied is to do what you believe is great work. And the only way to do great work is to love what you do. If you haven't found it yet keep looking. Don't settle. As with all matters of the heart, you'll know when you find it. (Steve Jobs)\",\n" +
                "\t\t\"Believe you can and you're halfway there. (Theodore Roosevelt)\",\n" +
                "\t\t\"Keep your face always toward the sunshine - and shadows will fall behind you. (Walt Whitman)\",\n" +
                "\t\t\"Perfection is not attainable, but if we chase perfection we can catch excellence. (Vince Lombardi)\",\n" +
                "\t\t\"If opportunity doesn't knock, build a door. (Milton Berle)\",\n" +
                "\t\t\"What we think, we become. (Buddha)\",\n" +
                "\t\t\"Clouds come floating into my life, no longer to carry rain or usher storm, but to add color to my sunset sky. (Rabindranath Tagore)\",\n" +
                "\t\t\"What lies behind you and what lies in front of you, pales in comparison to what lies inside of you. (Ralph Waldo Emerson)\",\n" +
                "\t\t\"Someone is sitting in the shade today because someone planted a tree a long time ago. (Warren Buffett)\",\n" +
                "\t\t\"No act of kindness, no matter how small, is ever wasted. (Aesop)\",\n" +
                "\t\t\"I believe that if one always looked at the skies, one would end up with wings. (Gustave Flaubert)\",\n" +
                "\t\t\"We know what we are, but know not what we may be. (William Shakespeare)\",\n" +
                "\t\t\"Let us sacrifice our today so that our children can have a better tomorrow. (A. P. J. Abdul Kalam)\",\n" +
                "\t\t\"There are two ways of spreading light: to be the candle or the mirror that reflects it. (Edith Wharton)\",\n" +
                "\t\t\"Let us remember: One book, one pen, one child, and one teacher can change the world. (Malala Yousafzai)\",\n" +
                "\t\t\"All you need is the plan, the road map, and the courage to press on to your destination. (Earl Nightingale)\",\n" +
                "\t\t\"Your personal life, your professional life, and your creative life are all intertwined. I went through a few very difficult years where I felt like a failure. But it was actually really important for me to go through that. Struggle, for me, is the most inspirational thing in the world at the end of the day - as long as you treat it that way. (Skylar Grey)\",\n" +
                "\t\t\"Your present circumstances don't determine where you can go; they merely determine where you start. (Nido Qubein)\",\n" +
                "\t\t\"I will love the light for it shows me that way, yet I will endure the darkness because it shows me the stars. (Og Mandino)\",\n" +
                "\t\t\"It is in your moments of decision that your destiny is shaped. (Tony Robbins)\",\n" +
                "\t\t\"Thousands of candles can be lighted from a single candle, and the life of the candle will not be shortened. Happiness never decreases by being shared. (Buddha)\",\n" +
                "\t\t\"The only journey is the one within. (Rainer Maria Rilke)\",\n" +
                "\t\t\"As we express our gratitude, we must never forget that the highest appreciation is not to utter words, but to live by them. (John F. Kennedy)\",\n" +
                "\t\t\"The bird is powered by its own life and by its motivation. (A. P. J. Abdul Kalam)\",\n" +
                "\t\t\"No matter what people tell you, words and ideas can change the world. (Robin Williams)\",\n" +
                "\t\t\"Don't judge each day by the harvest you reap but by the seeds you plant. (Robert Louis Stevenson)\",\n" +
                "\t\t\"I believe in pink. I believe that laughing is the best calorie burner. I believe in kissing, kissing a lot. I believe in being strong when everything seems to be going wrong. I believe that happy girls are the prettiest girls. I believe that tomorrow is another day and I believe in miracles. (Audrey Hepburn)\",\n" +
                "\t\t\"Shoot for the moon and if you miss you will still be among the stars. (Les Brown)\",\n" +
                "\t\t\"Let your life lightly dance on the edges of Time like dew on the tip of a leaf. (Rabindranath Tagore)\",\n" +
                "\t\t\"I hated every minute of training, but I said, 'Don't quit. Suffer now and live the rest of your life as a champion.' (Muhammad Ali)\",\n" +
                "\t\t\"We can't help everyone, but everyone can help someone. (Ronald Reagan)\",\n" +
                "\t\t\"God always gives His best to those who leave the choice with him. (Jim Elliot)\",\n" +
                "\t\t\"When you have a dream, you've got to grab it and never let go. (Carol Burnett)\",\n" +
                "\t\t\"If you believe in yourself and have dedication and pride - and never quit, you'll be a winner. The price of victory is high but so are the rewards. (Paul Bryant)\",\n" +
                "\t\t\"Let us make our future now, and let us make our dreams tomorrow's reality. (Malala Yousafzai)\",\n" +
                "\t\t\"A hero is someone who has given his or her life to something bigger than oneself. (Joseph Campbell)\",\n" +
                "\t\t\"When the sun is shining I can do anything; no mountain is too high, no trouble too difficult to overcome. (Wilma Rudolph)\",\n" +
                "\t\t\"Throw your dreams into space like a kite, and you do not know what it will bring back, a new life, a new friend, a new love, a new country. (Anais Nin)\",\n" +
                "\t\t\"If you always put limit on everything you do, physical or anything else. It will spread into your work and into your life. There are no limits. There are only plateaus, and you must not stay there, you must go beyond them. (Bruce Lee)\",\n" +
                "\t\t\"To love means loving the unloveable. To forgive means pardoning the unpardonable. Faith means believing the unbelievable. Hope means hoping when everything seems hopeless. (Gilbert K. Chesterton)\",\n" +
                "\t\t\"The measure of who we are is what we do with what we have. (Vince Lombardi)\",\n" +
                "\t\t\"It is never too late to be what you might have been. (George Eliot)\",\n" +
                "\t\t\"There is nothing impossible to him who will try. (Alexander the Great)\",\n" +
                "\t\t\"Two roads diverged in a wood and I - took the one less traveled by, and that has made all the difference. (Robert Frost)\",\n" +
                "\t\t\"From a small seed a mighty trunk may grow. (Aeschylus)\",\n" +
                "\t\t\"Give light, and the darkess will disappear of itself. (Desiderius Erasmus)\",\n" +
                "\t\t\"Love is a fruit in season at all times, and within reach of every hand. (Mother Teresa)\",\n" +
                "\t\t\"Be brave enough to live life creatively. The creative place where no one else has ever been. (Alan Alda)\",\n" +
                "\t\t\"If I have seen further than others, it is by standing upon the shoulders of giants. (Isaac Newton)\",\n" +
                "\t\t\"Follow your bliss and the universe will open doors where there were only walls. (Joseph Campbell)\",\n" +
                "\t\t\"Thinking: the talking of the soul with itself. (Plato)\",\n" +
                "\t\t\"Happiness resides not in possessions, and not in gold, happiness dwells in the soul. (Democritus)\",\n" +
                "\t\t\"How wonderful it is that nobody need wait a single moment before starting to improve the world. (Anne Frank)\",\n" +
                "\t\t\"When we seek to discover the best in others, we somehow bring out the best in ourselves. (William Arthur Ward)\",\n" +
                "\t\t\"With self-discipline most anything is possible. (Theodore Roosevelt)\",\n" +
                "\t\t\"To the mind that is still, the whole universe surrenders. (Lao Tzu)\",\n" +
                "\t\t\"Today is the only day. Yesterday is gone. (John Wooden)\",\n" +
                "\t\t\"Your big opportunity may be right where you are now. (Napoleon Hill)\",\n" +
                "\t\t\"The power of imagination makes us infinite. (John Muir)\",\n" +
                "\t\t\"Out of difficulties grow miracles. (Jean de la Bruyere)\",\n" +
                "\t\t\"What makes the desert beautiful is that somewhere it hides a well. (Antoine de Saint-Exupery)\",\n" +
                "\t\t\"Tomorrow is the most important thing in life. Comes into us at midnight very clean. It's perfect when it arrives and it puts itself in our hands. It hopes we've learning something from yesterday. (John Wayne)\",\n" +
                "\t\t\"If the world seems cold to you, kindle fires to warm it. (Lucy Larcom)\",\n" +
                "\t\t\"How glorious a greeting the sun gives the mountains. (John Muir)\",\n" +
                "\t\t\"When you get into a tight place and everything goes against you, till it seems as though you could not hang on a minute longer, never give up then, for that is just the place and time that the tide will turn. (Harriet Beecher Stowe)\",\n" +
                "\t\t\"Happiness is a butterfly, which when pursued, is always beyond your grasp, but which, if you will sit down quietly, may alight upon you. (Nathaniel Hawthorne)\",\n" +
                "\t\t\"Whoever is happy will make others happy too. (Anne Frank)\",\n" +
                "\t\t\"In a gentle way, you can shake the world. (Mahatma Gandhi)\",\n" +
                "\t\t\"Don't limit yourself. Many people limit themselves to what they think they can do. You can go as far as your mind lets you. What you believe, remember, you can achieve. (Mary Kay Ash)\",\n" +
                "\t\t\"We can change our lives. We can do, have, and be exactly what we wish. (Tony Robbings)\",\n" +
                "\t\t\"Even if I knew that tomorrow the 'would' would go to pieces, I would still plant my apple tree. (Martin Luther)\",\n" +
                "\t\t\"Memories of our lives, of our works and our deeds will continue in others. (Rosa Parks)\",\n" +
                "\t\t\"The things that we love tell us what we are. (Thomas Aquinas)\",\n" +
                "\t\t\"Somewhere, something incredible is waiting to be known. (Sharon Begley)\",\n" +
                "\t\t\"The glow of one warm thought is to me worth more than money. (Thomas Jefferson)\",\n" +
                "\t\t\"If a man does not keep pace with his companions, perhaps it is because he hears a different drummer. Let him step to the music which he hears, however measured or far away. (Henry David Thoreau)\",\n" +
                "\t\t\"Accept the things to which fate binds you, and love the people whom fate brings you together, but do so with all your heart. (Marcus Aurelius)\",\n" +
                "\t\t\"If we did all the things we are capable of, we would literally astound ourselves. (Thomas A. Edison)\",\n" +
                "\t\t\"Each day provides its own gifts. (Marcus Aurelius)\",\n" +
                "\t\t\"Keep your feet on the ground, but let your heart soar as high as it will. Refuse to be average or to surrender to the chill of your spiritual environment. (Arthur Helps)\",\n" +
                "\t\t\"Let us dream of tomorrow where we can truly love from the soul, and know love as the ultimate truth at the heart of all creation. (Michael Jackson)\",\n" +
                "\t\t\"You change your life by changing your heart. (Max Lucado)\",\n" +
                "\t\t\"A champion is someone who gets up when he can't. (Jack Dempsey)\"\n" +
                "\t]\n" +
                "}";
    }
}
