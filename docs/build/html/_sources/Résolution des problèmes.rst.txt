************************
Résolution des problèmes
************************

Schémas de conception technique
===============================
A partir du modèle de domaine et des users stories ainsi que de leur diagramme de séquence, vous trouverez ci dessous un diagramme de classe en UML qui permettra de comprendre le fonctionnement, mais surtout de respecter et adopter le langage omniprésent de l’application.

Glossaire
=========

Afin d'avoir une vision plus explicite de l'application, vous trouverez ci dessous le diagramme de classe

Diagramme de classe 
-------------------

.. image:: _static/diagrams/Class_diagram/Diagram_class.png
    :width: 100%
    :alt: Diagramme de classe 
    :name: Diagramme de classe 

Spécifications techniques
=========================

3.3.1 Choix de l'architecture
-----------------------------

Cette application se repose sur une architecture 3 tiers pour exposer un REST API. Elle utilise, entre autre, le langage Java, le framework SpringBoot, ainsi que Gradle pour gérer au mieux ses dépendances.

De par son architecture, elle permettra donc:

 * De communiquer plus facilement avec une future implémentation d'un front-end et celà grâce à l'exposition de son REST API.
 * D'obtenir rapidement des informations par le biais de requêtes sur des API distantes.
 * D'être déployer aisément et de fonctionner de manière autonome.

.. warning::
    
    Il est tout de même important de préciser, qu'actuellement, cette version d'application ne comporte pas de base de donnée (celle ci est "mockée" grâce à l'utilisation d'une HashMap internalUserMap) et, en ce qui concerne l'appel a des API distantes, ce comportement est remplacé par l'utilisation de librairies embarquées dans l'application : GpsUtils, RewardsCentral et TripPricer.L'appel à leur methodes simule une requête avec un temps de réponse plus ou moins long...
 
Ci dessous un schéma de l'architecture permettant de mettre en avant le fonctionnement des différentes couches:



3.3.2 Frameworks et IDE utilisés
--------------------------------
    * SpringBoot 2.5.4
    * ModelMapper 3.1.0
    * Java 8
    * Gradle 7.2
    * JUnit,Hamcrest
    * Jacoco
    * Log4j2

.. _resolution-pb:

Solutions aux problèmes relatés au §1.1 
=======================================

Calendrier prévisionnel et exigences
====================================

Vous trouverez ci dessous un lien vers la feuille de route (Jira) utilisée pour répondre aux besoins du projet et aux futures exigences des prochaines versions à venir...

.. image:: _static/tourGuide_roadMap.png
    :target: https://doriandelaval.atlassian.net/jira/software/projects/TG/boards/2/roadmap
    :alt: Jira feuille de route

