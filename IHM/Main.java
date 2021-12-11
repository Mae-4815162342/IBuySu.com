package IHM;

import System.IBuySu;

public class Main {
    private static boolean exiting = false;
    private static IBuySu system;

    public static void main(String[] args) {
        System.out.println(PromptUtils.b("Bienvenue sur IBuySu.com, votre site d'achat-vente en ligne !"));
        System.out.println(PromptUtils.yel("Connexion en cours..."));
        
        try {
            system = IBuySu.getSystem();
        } catch (Exception e) {
            PromptUtils.printError("Echec de la connexion\nFermeture du système");
            exit();
        }

        while (!exiting) {
            traiterChoix(IHM.deroulerMenu(PromptUtils.yel("Que désirez-vous faire ?"), system.getMenu()));
        }
        System.out.println(PromptUtils.b("A bientôt sur IBuySu.com!"));
        return;
    }

    public static void exit() {
        exiting = true;
    }

    public static void traiterChoix(String choix) {
        switch (choix) {
            case "Recherche":
                system.rechercher();
                break;
            case "Inscription Vendeur":
                system.inscriptionVendeur();
                break;
            case "Inscription Acheteur":
                system.inscriptionAcheteur();
                break;
            case "Connexion":
                system.connexion();
                break;
            case "Déconnexion":
                system.deconnexion();
                break;
            case "Participer à une enchère":
                system.acheterObjetEnchere();
                break;
            case "Acheter un objet":
                system.acheterUnObjet();
                break;
            case "Evaluer un utilisateur":
                system.evaluerUnUtilisateur();
                break;
            case "Quitter":
                exit();
                break;
            default:
                break;
        }
    }
}
