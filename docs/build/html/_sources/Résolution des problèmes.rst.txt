.. _resolution-pb:

************************
Résolution des problèmes
************************

Dans ce chapitre, nous allons reprendre les problèmatiques exposées au §1.1.

Pour chacune d'entre elles, nous verrons la démarche suivie pour la résolution de ces dernières, les changements et tests mis en oeuvre, ainsi que le résultat obtenu à la suite des refactorisations de code.

Préambule
=========

La résolution des problèmes rencontrés a été effectuée avec l'outil Jira, pour suivre une démarche agile. Chaque sprint (épic) corresponds à une étape de correction, lesquels sont constitués de tickets permettant de détailler chaque point important du processus d'amélioration de l'application. 

.. image:: _static/jira/outil_Jira.png
    :width: 100%
    :alt: Feuille de route TourGuide 
    :name: Feuille de route TourGuide 

Mise à niveau de l'application
==============================

Avant de commencer, il a fallu mettre à jour l'application et ses dépendances au moyen de Gradle. A aujourd'hui, l'application utilise Gradle version 7.2 ce qui permet de rendre visible toutes les tasks disponibles pour ce projet (ce qui n'était pas le cas avec l'ancienne version...)

Les versions de SpringBoot et de Junit ont également été upgradée de sorte de pouvoir utiliser les dernières annotations et, par exemple,  utiliser des test paramétrés plus facilement...

Nous sommes aussi passé à la jdk 11 pour pouvoir utiliser JVisualVM et profiler notre application pour résoudre les lenteurs relatées par les utilisateurs.


Problèmatique rencontrée
------------------------


Une fois l'application upgradée et lancée , l'exception suivante est apparue immédiatement :

.. code-block:: shell

    java.lang.NumberFormatException: For input string: "-166,341300"
    at sun.misc.FloatingDecimal.readJavaFormatString(FloatingDecimal.java:2043)
    at sun.misc.FloatingDecimal.parseDouble(FloatingDecimal.java:110)
    at java.lang.Double.parseDouble(Double.java:538)
    at gpsUtil.GpsUtil.getUserLocation(GpsUtil.java:30)
    at tourGuide.service.TourGuideService.trackUserLocation(TourGuideService.java:87)
    at tourGuide.TestRewardsService.userGetRewards(TestRewardsService.java:35)
 

Explication
-----------

Lors de l’appel à la methode  trackUserLocation() de TourGuideService, on utilise la methode getUserLocation() de GpsUtil qui parse des longitudes et latitudes sous forme de String mais avec une virgule puisque la Locale de notre application n'étant pas definie est,par défaut, en français (ex: 31,765747).


Résolution
----------

Nous avons setter la user Locale en anglais pour avoir des doubles sous forme de string avec un point et non une virgule. Ainsi le parse en double des longitudes et latitudes ne lève plus d'exception.

Ajout donc dans application.properties de: 

.. code-block:: java

    spring.web.locale=en_EN


Amélioration des performances
=============================

Dans cette  section, nous parlons de l'amélioration des performances de l'application en général.

Que ce soit pour l'appel à **GspUtil** ou à **RewardsCentral**, nous avons constaté une lenteur du à l'appel de certaines méthodes qui renvoient une réponse après un certain temps (utilisation de la methode sleep() pour simuler ce temps de réponse).



Problèmatique rencontrée
------------------------

Tout d'abord, afin de déterminer au mieux les méthodes qui ralentissaient l'application, nous avons utiliser l'application JVisualVM pour trouver et vérifier d'ou venaient ces lenteurs.

Ci dessous, une impression d'écran de JVisualVM profilant notre application: 

.. image:: _static/JvisualVm/visualVm_snapShoot1.png
    :width: 100%
    :alt: snapShoot du profilage de JVisualVm 
    :name: JVisualVm_snapShoot 



Nous avons donc pu constater que les lenteurs provenaient donc des méthodes suivantes:

    * **getUserLocation()** de GpsUtil 
    * **calculateRewards()** de RewardsService



Explication
-----------
Ces lenteurs contatées par les utilisateurs s'explique par le fait que l'application appelait ces méthodes de manière séquentielle et que ces dernières renvoyaient leur retour avec un temps de réponse aléatoire plus ou moins long (méthode sleep() de substitution).

Par conséquent, plus le nombre d'utilisateurs devenait important et plus le temps de réponse l'était aussi (cf les graphes de performances ci dessous avant refactorisation du code)



Résolution
----------
 
Après avoir donc identifier les méthodes fautives, nous sommes passés à l'étape de la résolution...


Modification de l'architecture existante
`````````````````````````````````````````

Pour respecter la responsabilité unique du principe SOLID et l'architecture MVC en 3 couches distinstes, nous avons crée 3 services dédiés uniquement à la gestion des appels au librairies GspsUtil , RewardsCentral et TripPricer.

Ci dessous, respectivement les trois services nouvellement crés:

    * **GpsUtilService**
    * **RewardsService**
    * **TripPricerService**

En outre, le service TourGuideService, au démarrage de l'application, instancie respectivement ces trois services.


Utilisation de l'API Concurrency de Java 8
``````````````````````````````````````````

Pour palier aux problèmes de lenteur lors de l'appel aux méthodes getUserLocation() et calculateRewards(), nous avons utilisé `l'API Concurrency de Java 8 <https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html>`_, de sorte à plutôt effectuer des "requêtes asynchrones" que de faire, des appels directs aux méthodes incriminées de manière séquentielle...

Ainsi, en utilisant des `CompletableFutures <https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html>`_ pour pouvoir gérer simplement le retour des méthodes, l'application utilise donc maintenant un paragdime concurrentiel. Ce qui permet d'améliorer nettement, la rapidité de cette dernière, quelque soit le nombre d'utilisateurs...

De plus, l'utilisation des CompletableFutures , nous permet aussi, contrairement aux Futures, de pouvoir plus facilement enchainer d'autres taches, une fois le retour acquis et cela, grâce aux méthodes prédéfinies de l'interface `CompletionStage <https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletionStage.html>`_

.. note::
    Pour pouvoir utiliser la concurrence, il a fallu modifier donc, quelque peu les services:

        * **Ajout d'une instance d'ExecutorService** pour gérer correctement les threads lancés par les CompletableFutures
        * **Utilisation d'un CompletableFuture** pour chaque appel aux méthodes incriminées
        * **Gestion des retours asynchrones** avec enchaînement d'autres actions à effectuer avec ou sans répoonses.
        * **Gestion de l'accés a des ressources partagées** entre les différents threads



GpsUtilService
``````````````

Les modifications de l'appel à l'ancienne méthode getUserLocation() de GpsUtils se sont fait en deux étapes:

Refactorisation de TourGuideService:
'''''''''''''''''''''''''''''''''''''
on utilise directement une instance d'ExcecutorService tourGuideServiceExecutor pour lancer un thread permettant d'appeler la nouvelle méthode getLocation() de notre GpsUtilService. Ceci est  particulièrement intéressant lors de l'éxecution du test de performance, puisque cela permet, au travers des logs, de distinguer correctement les appels a getLocation() provenant soit du Tracker, soit de TourGuideService même.

.. code:: java

    /**
    * Track the location of a user
    * 
    * @param user user to be tracking
    */
    public void trackUserLocation(User user) {
        try {
            tourGuideServiceExecutor.submit(() -> {
                logger.debug("\033[32m - trackUserLocation({})", user.getUserName());
                gpsUtilService.getLocation(user, this);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


Refactorisation de GspUtilService: 
''''''''''''''''''''''''''''''''''

L'appel a la méthode getUserLocation() de GspUtils se fait maintenant en utilisant une CompletableFuture qui avec sa méthode supplyAsync() récupére, de manière asynchrone, le retour de getUserLocation().  

.. code:: java

    public void getLocation(User user, TourGuideService tourGuideService) {

    CompletableFuture.supplyAsync(() -> {
        logger.debug("\033[33m - gpstUtils.getUserLocation({})", user.getUserName());
        return gpsUtil.getUserLocation(user.getUserId());
    }, gpsExecutorService).thenAccept(location -> {
        logger.debug("\033[33m - tourGuideService.saveTrackedUserLocation({},{})", user.getUserName(), location);
        tourGuideService.saveTrackedUserLocation(user, location);
    });

Une fois le retour récupéré, on enchaîne avec la suite du process implémenté dans la méthode de TourGuideService saveTrackedUSerLocation(): 

    * Ajouter la dernière localité visitée a l'historique de l'utilisateur
    * Caluler les points de récompense pour cette visite
    * Mettre à jour la HasMap trackingUsersProgress partagée avec le Tracker pour définir que l'utilisateur a bien été "localisé" 

.. code:: java

    /**
    * Add the last Tracked location to user's list of visited Locations, Calulate the rewards for user
    * visited location and add it ( point & informations) to his userRewards update the map
    * UserTrackingProgress to informe the tracker of asynchronous progression of tracking
    * 
    * @param user the user to be tracking
    * @param visitedLocation it's last visited location
    */
    public void saveTrackedUserLocation(User user, VisitedLocation visitedLocation) {
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        tracker.updateUserTrackingProgress(user);
    }

Une fois ces modifications appliquées, notre application maintenant fonctionne en utilisant un paragdime concurrentiel.

Cependant, étant donné que Tracker et TourguideService (notamment pendant les tests de performances) utilisent tous les deux, de manière asynchrone, une même ressource qui n'est autre que la liste des localités visitées pour chaque utilisateur, il a fallu introduire dans la classe User un ReentrantLock pour gérer correctement le partage de cette ressource entre l'instance de Tracker et de TrouguideService.

Ci dessous, l'implémentation du ce ReentrantLock dans User.java:

.. code:: java

    public class User {
        private final UUID userId;
        private final String userName;
        /.../
        private Lock userVisitedLocations = new ReentrantLock();
        /.../

        public void addToVisitedLocations(VisitedLocation visitedLocation) {
		    userVisitedLocations.lock();
            try {
                visitedLocations.add(visitedLocation);
            } finally {
                userVisitedLocations.unlock();
            }
        public List<VisitedLocation> getVisitedLocations() {
            userVisitedLocations.lock();
            try {
                return visitedLocations;
            } finally {
                userVisitedLocations.unlock();
            }
        }
        public void clearVisitedLocations() {
            userVisitedLocations.lock();
            try {
                visitedLocations.clear();
            } finally {
                userVisitedLocations.unlock();
            }
        }
	}




RewardsService
``````````````

Pour l'appel à RewardsCentral, nous avons refactorisé de la même manière la méthode calculateRewards() de sorte, à également, faire appel à cette dernière de manière asynchrone.
N'ayant pour cette fois, pas besoin dans les logs de déterminer quelle instance appelle cette méthode, nous n'avons pas fait appel à un thread dans TourguideService...


Refactorisation de RewardsService: 
'''''''''''''''''''''''''''''''''''

La méthode calculateRewards() a été, pour faciliter sa lecture, cinder en deux partie:

    * une première partie pour vérifier si l'utilisateur a visité une localité proche d'une attraction existante: **la méthode calculateRewards()**
    * une seconde partie pour récupérer les points de récompense suite à cette visite proche d'une attraction et fournir a l'utilisateur une instance de Rewards comprennant la localité visitée , l'attraction proche de cette dernière et le nombre de points de récompense obtenus: **la méthode setUserRewards()**

.. code:: java

    public void calculateRewards(User user) {
		logger.debug("\033[35m - calculateRewards({}) ", user.getUserName());
		CopyOnWriteArrayList<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());
		List<Attraction> attractions = gpsUtilService.getAttractions();

		for (VisitedLocation visitedLocation : userLocations) {
			for (Attraction attraction : attractions) {
				if (user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
					if (nearAttraction(visitedLocation, attraction)) {
						logger.debug("\033[35m - setUserRewards({}, {}, {}) ", attraction.attractionName, visitedLocation, user.getUserName());
						setUserRewards(attraction, visitedLocation, user);
					}
				}
			}
		}
	}

    private void setUserRewards(Attraction attraction, VisitedLocation visitedLocation, User user) {
		CompletableFuture.supplyAsync(() -> {
			logger.debug("\033[35m - getRewardPoints({}, {}) ", attraction.attractionName, user.getUserName());
			return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
		}, rewardsExecutorService).thenAccept(rewardsPoint -> {
			logger.debug("\033[35m - addUserReward({}, {}, {}, {}) ", user.getUserName(), visitedLocation, attraction.attractionName, rewardsPoint);
			user.addUserReward(new UserReward(visitedLocation, attraction, rewardsPoint));
		});
	}

.. note::

    Concernant la liste de localité visitées de l'utilisateur, une fois la refactorisation du code effectuée, nous avons constaté qu'une exception de concurence était levée lors de l'appel à calculateRewards(). Cette exception apparaissait car plusieurs threads voulaient parcourir avec un iterator la même liste de localités visitées userLocation en même temps.
    Pour résoudre le problème nous avons changer le type de userLocation **ArrayList en CopyOnWriteArrayList**. Ainsi chaque thread lorsqu'il appelle la méthode calculateRewards(), travaille non plus sur l'unique liste userLocation mais sur une copie.


Refactorisation des tests
-------------------------

Toutes ces modifications apportés au code pour implémenter un paragdime concurrentiel, ont forcémenent fait passer les tests en échecs.
Nous avons donc procéder à une refactorisation de ces derniers...


HighVolumeGetLocation()
```````````````````````

ci dessous, les principaux changements effectués:

* Modification du constructeur de TourguideService. Ce dernier prends en arguments maintenant les trois services décris plus haut soit:GpsUtilService, RewardsService et TripPricerService
* Ajout d'une interruption du main thread pour chaque utilisateur une fois l'appel à la méthode trackUserLocation() de TourGuideService terminée car il faut attendre que l'application ait bien attribué 5 localités visitées pour chacun d'entre eux ( 4 localités attribué par le Tracker et 1 par l'appel de tourGuideService) avant d'arreter l'application et vérifier le temps obtenu.

.. code:: java

    public void highVolumeTrackLocation() {

		// Users should be incremented up to 100,000, and test finishes within 15
		// minutes
		InternalTestHelper.setInternalUserNumber(10);
		
		System.setProperty("logFileName", "highVolumeTrackLocation-" + InternalTestHelper.getInternalUserNumber());
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();
		Logger logger = LogManager.getLogger("testPerformance");
		Logger rootLogger = LogManager.getRootLogger();

		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		TripPricerService tripPricerService = new TripPricerService(new TripPricer());

		rootLogger.info("----------------------HightVolumeTrackLocation with {} users-----------------------\t", InternalTestHelper.getInternalUserNumber());
		
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService,tripPricerService);

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		List<User> allUsers = tourGuideService.getAllUsers();

		for (User user : allUsers) {
			logger.debug("\033[36m - tourGuideService.trackUserLocation({}) ", user.getUserName());
			tourGuideService.trackUserLocation(user);
		}
		for (User user : allUsers) {
			while (user.getVisitedLocations().size() < TourGuideService.INITIAL_NUMBER_OF_VISITED_LOCATIONS +1) {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		stopWatch.stop();
		
		tourGuideService.addShutDownHook();

		rootLogger.info("highVolumeTrackLocation: Time Elapsed: {} seconds", TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));

		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));

	}





HighVolumeGetRewards()
``````````````````````
Ci dessous, les principaux changements effectués:

* Modification du constructeur de TourguideService. Ce dernier prends en arguments maintenant les trois services décris plus haut soit:GpsUtilService, RewardsService et TripPricerService
* Suppression de l'appel à la méthode calculateRewards() de RewardsService car elle est inutile puisque lors de l'instanciation de tourGuidesService, le tracker tourne déjà. Il suffit simmplement, pour chaque utilisateur, de vider la liste des localités visitées , ajouter une nouvelle locatisation (ici une attraction pour vérifier que l'on ait bien des rewards points attribués a la fin) et laisser le tracker faire son travail.
* Ajout d'une interruption du main thread pour attendre que tous les utilisateurs ont bien reçu des rewards points.

.. code:: java

    @Test
	public void highVolumeGetRewards() {

		// Users should be incremented up to 100,000, and test finishes within 20
		// minutes
		InternalTestHelper.setInternalUserNumber(10);
		
		System.setProperty("logFileName", "highVolumeGetRewards-" + InternalTestHelper.getInternalUserNumber());
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();
		Logger logger = LogManager.getLogger("testPerformance");
		Logger rootLogger = LogManager.getRootLogger();

		GpsUtilService gpsUtilService = new GpsUtilService(new GpsUtil());
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		TripPricerService tripPricerService = new TripPricerService(new TripPricer());

		rootLogger.info("----------------------highVolumeGetRewards with {} users-----------------------", InternalTestHelper.getInternalUserNumber());
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService,tripPricerService);

		Attraction attraction = gpsUtilService.getAttractions().get(0);
		List<User> allUsers = tourGuideService.getAllUsers();

		allUsers.forEach(u -> {
			VisitedLocation firstAttraction = new VisitedLocation(u.getUserId(), attraction, new Date());
			logger.debug("\033[36m - addToVisitedLocations({}) to user: {} ",  attraction.attractionName, u.getUserName());
			u.getVisitedLocations().clear();
			u.addToVisitedLocations(firstAttraction);
		});
		
		for (User user : allUsers) {
			while (user.getUserRewards().isEmpty()) {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		for (User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}

		stopWatch.stop();

		tourGuideService.addShutDownHook();

		rootLogger.info("highVolumeGetRewards: Time Elapsed: {} seconds", TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));

		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));

	}


Résultat obtenus
----------------


