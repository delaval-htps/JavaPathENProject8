*************************
Présentation du projet
*************************

TourGuide est une application Spring Boot et une pièce maîtresse du portfolio
d'applications TripMaster. Elle permet aux utilisateurs de voir quelles sont les
attractions touristiques à proximité et d’obtenir des réductions sur les séjours d’hôtel
ainsi que sur les billets de différents spectacles.


Objectifs du projet
===================
*Décrivez les objectifs du projet (2 à 3 phrases), y compris le(s) problème(s) résolu(s).*

Dans l'état actuel des choses, TourGuide est tout simplement trop lent pour servir de nombreux clients. Les utilisateurs l’ont remarqué et se plaignent des mauvaises
performances. 

Il faudrait donc :

* **Améliorer les performances de gpsUtil** 
    * ce service collecte l'emplacement du téléphone mobile ou de l’ordinateur portable de l'utilisateur. En cas d'utilisation intensive, gpsUtil connaît des temps de réponse lents

* **Améliorer les performances de RewardsCentral**
    * ce service fait appel à un réseau de fournisseurs pour regrouper un ensemble de valeurs et déterminer les récompenses offertes pour chaque attraction touristique. La collecte de ces offres prend un temps indéterminé, car TourGuide n’a aucun contrôle sur le taux de réponse des partenaires du réseau. Lors des pics d’utilisation, certains utilisateurs se sont plaint d’avoir reçu les récompenses le lendemain voire le surlendemain.

* **Corriger des défauts signalés** 
    * Certains tests unitaires échouent par intermittence
    * Les offres de voyage ne correspondaient pas exactement à leurs préférences, par exemple au niveau du nombre d’enfants ou de la durée du séjour ;
    * Les recommandations d'attractions touristiques ne sont pas soit reçcus par les utilisateurs ou nne sont pas pertinentes
    * Ajout d'une nouvelle fonctionnalité: regrouper tous les emplacements de tous les utilisateurs pour les visualiser et  identifier si un schéma logique ou répétitif s'en dégage au fil du temps

Hors du champ d’application
===========================
*Découvrez les objectifs qui ont été envisagés, mais ne sont pas couverts par ce projet.*

Le projet consiste à améliorer les performances de l'application. Il serait envisageable également pour celà de choisir certains outils permettant d'optimiser l'accés au données stockés en bdd (elasticSearch...).Voir même  d'optimiser les requétes pour accélerer les temps de réponse. Mais cela n'est pas pour le moment directement demandé car la bdd est simulée. Ceci étant, cela pourrait également faire l'objet d'axe d'amélioration par a suite.

Mesures du projet
=================

*Indiquez comment vous allez mesurer le succès du projet.*

Les corrections de bugs et les nouvelles fonctionnalités devront être évaluées avec une suite de tests.
On utilisera les tests de performances existants au fur et à mesure de la résolution des problèmatiques. Ces derniers nous permettront de mettre à jour le fichier Excel `Graphiques et métriques des performances de TourGuide` afinde vérifier si les objectifs demandés soient bien atteints.