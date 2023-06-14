****************
Caractéristiques
****************

Dans ce chapitre, vous trouverez l'ensemble des fonctionnalités que propose l'application TourGuide à ses utilisateurs ainsi qu'à ses administrateurs. De part sa conception (REST API), chacunes de ses fonctionnalités n'est accessible aujourd'hui que depuis un endpoint bien prédéfini. Il est biensur évident que pour, une question de faciliter d'utilisation avec un téléphone mobile, l'application se verra plus tard aggrémenter d'une IHM coté frontEnd qui utilisera ces endpoints...

.. raw:: latex

        \newpage

Fonctionnalités du projet
=========================

Pour permettre une compréhension globale des fonctionnalités de l’application nécessitant une amélioration de performance de temps, nous avons définit **Le diagramme de cas d'utilisation** suivant:

.. image:: _static/diagrams/User_case_diagram/user_case.png
    :width: 100%
    :alt: User case diagram
    :name: User_case_diagram
    :class: page-break


User Stories & critéres d'acceptation
=====================================

A partir de ce diagramme de cas d'utilisation, nous pouvons donc déterminer les user stories et leurs critéres d'acceptation suivantes:

1. **En tant qu'utilisateur , je veux pouvoir accéder à l'application'**

        * **Scénario** l'utilisateur a accés à internet.
        * **Etant donné** que je suis un utilisateur.
        * **Lorque** je rentre l'URL "http://localhost:8080".
        * **Alors** j'accède a la page d'accueil de TrouGuide".


2. **En tant qu'utilisateur , je peux visualiser ma dernière localisation**  
        
        * **Scénario** l'utilisateur a accés à internet.
        * **Etant donné** que je suis un utilisateur.
        * **Lorque** je rentre l'URL "http://localhost:8080/getLocation?userName=<mon nom d'utilisateur>".
        * **Alors** j'accède a la page d'accueil de TourGuide".



3. **En tant qu'utilisateur , je peux visualiser les 5 attractions les plus proches de moi**  
        
        * **Scénario** l'utilisateur a accés à internet.
        * **Etant donné** que je suis un utilisateur.
        * **Lorque** je rentre l'URL "http://localhost:8080/getNearbyAttractions?userName=<mon nom d'utilisateur>".
        * **Alors** j'accède a la page me donnant la liste des 5 attractions les plus proches de ma localisation (triées par distance).



4. **En tant qu'utilisateur , je peux consulter mes récompenses**  

        * **Scénario** l'utilisateur a accés à internet.
        * **Etant donné** que je suis un utilisateur.
        * **Lorque** je rentre l'URL "http://localhost:8080/getRewards?userName=<mon nom d'utilisateur>".
        * **Alors** j'accède a la page m'affichant la liste de mes récompenses.



5. **En tant qu'utilisateur , je peux voir mes préférences**  

        * **Scénario** l'utilisateur a accés à internet.
        * **Etant donné** que je suis un utilisateur.
        * **Lorque** je rentre l'URL "http://localhost:8080/getUserPreferences?userName=<mon nom d'utilisateur>".
        * **Alors** j'accède a la page m'affichant mes préférences.



6. **En tant qu'utilisateur , je peux modifier mes préférences**  

        * **Scénario** l'utilisateur a accés à internet.
        * **Etant donné** que je suis un utilisateur.
        * **Lorque** je rentre l'URL "http://localhost:8080/setUserPreferences?userName=<mon nom d'utilisateur>" et ajoute dans le corps de la requete sous format json mes préférences.
        * **Alors** l'application enregistre mes préférences si tous mes champs sont corrects.Dans le cas contraire, si une ou plusieurs valeur(s) n'est (ne sont) pas valides, j'ai pour réponse une indication sur la(les) valeur(s) erronée(s) pour pouvoir la(les) modifier.



7. **En tant qu'utilisateur , je peux obtenir des propositions de voyage déterminées en fonction de mes préférences**  

        * **Scénario** l'utilisateur a accés à internet.
        * **Etant donné** que je suis un utilisateur.
        * **Lorque** je rentre l'URL "http://localhost:8080/getTripDeals?userName=<mon nom d'utilisateur>".
        * **Alors** j'accède a la page m'affichant une liste de propositions de voyage prennant en compte mes préférences.



8. **En tant qu'administrateur , je peux obtenir la liste de toutes les localisations de tous les utilisateurs**

        * **Scénario** l'utilisateur a accés à internet.
        * **Etant donné** que je suis un administrateur.
        * **Lorque** je rentre l'URL "http://localhost:8080/getAllUserLocations".
        * **Alors** j'accède a la page m'affichant une liste de tous les utilisateurs avec l'historique de leurs localisations.



9. **En tant qu'administrateur , je peux obtenir la liste de toutes les dernières localisations de tous les utilisateurs**

        * **Scénario** l'utilisateur a accés à internet.
        * **Etant donné** que je suis un administrateur.
        * **Lorque** je rentre l'URL "http://localhost:8080/getAllCurrentlLocations".
        * **Alors** j'accède a la page m'affichant une liste de tous les utilisateurs avec leur dernière localisation.
