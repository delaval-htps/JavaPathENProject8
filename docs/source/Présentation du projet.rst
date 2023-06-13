*************************
Présentation du projet
*************************

TourGuide est une application Spring Boot et une pièce maîtresse du portfolio
d'applications TripMaster. Elle permet aux utilisateurs de voir quelles sont les
attractions touristiques à proximité et d’obtenir des réductions sur les séjours d’hôtel
ainsi que sur les billets de différents spectacles.


Objectifs du projet
===================

Dans l'état actuel des choses, TourGuide fonctionne correctement mais elle est tout simplement trop lente pour servir de nombreux clients.
Les utilisateurs l’ont remarqué, se plaignent des mauvaises performances et ont remonté quelques dysfonctionnements... 

Il faudrait donc :

#. **Améliorer les performances de gpsUtil** 
    
    * Ce service collecte l'emplacement du téléphone mobile ou de l’ordinateur portable de l'utilisateur. En cas d'utilisation intensive, gpsUtil connaît des temps de réponse lents.


#. **Améliorer les performances de RewardsCentral**
    
    * Ce service fait appel à un réseau de fournisseurs pour regrouper un ensemble de valeurs et déterminer les récompenses offertes pour chaque attraction touristique. La collecte de ces offres prend un temps indéterminé, car TourGuide n’a aucun contrôle sur le taux de réponse des partenaires du réseau. Lors des pics d’utilisation, certains utilisateurs se sont plaint d’avoir reçu les récompenses le lendemain voire le surlendemain.


#. **Corriger des défauts signalés** 
    
    * Certains tests unitaires échouent par intermittence
    * Les offres de voyage ne correspondaient pas exactement à leurs préférences, par exemple au niveau du nombre d’enfants ou de la durée du séjour.
    * Les recommandations d'attractions touristiques ne sont pas soit reçues par les utilisateurs ou ne sont pas pertinentes.
    

#. **Ajouter une nouvelle fonctionnalité**
    
    * Regrouper tous les emplacements de tous les utilisateurs pour les visualiser et  identifier si un schéma logique ou répétitif s'en dégage au fil du temps

#. **Améliorer le process qualité**

    * Mettre en place une chaîne de build (pipeline CI/CD GitLab) qui permettra d'exécuter la compilation des classes, de s'assurer de la non régréssion des tests et d'obtenir un artefact valide du projet.

Tous ces améliorations sont traitées dans le chapitre 4 :ref:`resolution-pb`.
Vous y trouverez la démarche utilisée, le traitement des erreurs, et les tests réalisés.

Hors du champ d’application
===========================

Le projet consiste à améliorer les performances de l'application. 

A aujourd'hui, TourGuide n'utilise pas de base de donnnée pour stocker et récupérer les données de ses utilisateurs. Il serait envisageable, par la suite, de choisir certains outils permettant d'optimiser l'accés au données stockés en bdd (elasticSearch...), et même,  d'optimiser les requétes pour accélerer les temps de réponse. Mais cela n'est pas pour le moment directement demandé car la bdd est actuellement simulée. Ceci étant, cela pourrait également faire l'objet d'axe d'amélioration.

Compte tenu de sa conception actuelle (TourGuide n'est autre qu'un REST API coté back-end), il en va de soit qu'il faudra par la suite, implémenter une IHM coté front-end pour rendre plus facile son utilisation... 


Mesures du projet
=================

On utilisera les tests de performances existants au fur et à mesure de la résolution des problèmatiques de performances. Ces derniers nous permettront de mettre à jour le fichier Excel `Graphiques et métriques des performances de TourGuide` afin de vérifier si les objectifs demandés ont bien été atteints.

La correction de bugs, la nouvelle fonctionnalitée devront être évaluées avec une suite de tests.

La mise en place d'un pipeline CI/CD sur le Gitlab de l'application, nous permettra également de vérifier, tout le long de l'amélioration de l'application, la non régréssion des tests.