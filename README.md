# TP HTTP-SERVEUR
## Lancement du serveur
Pour compiler le serveur, se placer dans le dossier *src/src/http/server* et taper la commande :
```
javac WebServer.java
```
Pour lancer le serveur taper la commande : 
```
java WebServer
```
Une fois le serveur lancé, il est possible de faire des requêtes de cinq types :
- GET
- HEAD
- POST  
- PUT 
- DELETE

## GET
### Faire une requête
Pour les requêtes de type GET, il est possible de les lancer depuis un navigateur de votre choix ou depuis POSTMAN.
Pour afficher la page d'index il faut taper l'url :```localhost:3000```. Cette page d'index contient des informations sur les méthodes utilisées.
Pour afficher un fichier il faut rajouter le nom de la ressource comme suit :
```localhost:3000/nomDeLaRessource```. 
### Emplacement des ressources
Les ressources se trouvent dans le sous-dossier *src/doc*.
###Type de ressources pour la méthode GET
Il est possible d'afficher dans le navigateur des ressources de type texte, image, vidéo, audio, js...
### Erreurs
Une page d'erreur s'affiche en cas de ressource inexistante.
## POSTMAN
Pour les méthodes suivantes nous conseillons l'utilisation de POSTMAN.
## HEAD
Les requêtes et types supportés par cette méthode sont les mêmes que pour la méthode GET.
## POST
Pour lancer cette requête il faut taper une nouvelle fois ```localhost:3000/nommDeLaRessource```.
La ressource dans la requête correspond au fichier dans lequel on va ajouter du contenu. Si la ressource n'existe pas elle sera créée, sinon le contenu sera ajouté en fin de fichier.
Le contenu doit être inséré dans le body de la requête, cela peut-être du texte tapé à la main (dans la partie "*raw*" de POSTMAN) ou un fichier texte (partie "*binary*").
Les ressources sont placées dans la sous-dossier *src/doc*.
## PUT
Cette méthode est la même que la méthode POST, à la différence près qu'on n'ajoute pas en fin de fichier mais on écrase ou crée la ressource.
## DELETE
Pour lancer cette requête il faut taper une nouvelle fois ```localhost:3000/nommDeLaRessource```. La ressource est celle à effacer. Si elle n'existe pas on affiche une page d'erreur.


