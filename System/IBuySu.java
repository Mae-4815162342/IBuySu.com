package System;

import IHM.IHM;
import IHM.PromptUtils;
import BDD.API;
import java.util.ArrayList;
import java.util.List;

public class IBuySu {
    private List<Categorie> categories = new ArrayList<Categorie>();
    private List<MotClef> motClef = new ArrayList<>();
    private List<Inscrit> users = new ArrayList<>();
    private Utilisateur user;
    private static IBuySu system;

    public List<MotClef> getMotClef() {
        return motClef;
    }

    public void setMotClef(List<MotClef> motClef) {
        this.motClef = motClef;
    }

    public List<Inscrit> getUsers() {
        return users;
    }

    public void setUsers(List<Inscrit> users) {
        this.users = users;
    }

    public List<Categorie> getCategories() {
        return categories;
    }

    public void setCategories(List<Categorie> categories) {
        this.categories = categories;
    }

    private IBuySu() throws Exception {
        user = new Utilisateur();
        try {
            API.setConnexion();
            API.fetchUsers(this);
        } catch (Exception e) {
            throw e;
        }
    }

    public static IBuySu getSystem() {
        if (system == null) {
            try {
                system = new IBuySu();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return system;
    }

    public String[] getMenu() {
        return user.getMenu();
    }

    public void connexion() {
        String[] formulaire = Inscrit.getFormulaireConnexion();
        String[] identifiants = IHM.remplirFormulaire(PromptUtils.b("Formulaire de connexion"), formulaire);
        Inscrit connecting = null;
        for (Inscrit user: users) {
            if(user.getMail().equalsIgnoreCase(identifiants[0])) {
                connecting = user;
            }
        }

        if (connecting == null) {
            PromptUtils.printError("Le mail ne correspond à aucun utilisateur");
            return;
        }

        if (!connecting.verifMdp(identifiants[1])) {
            PromptUtils.printError("Mot de passe incorrect");
            return;
        }

        user = connecting;
        PromptUtils.printSuccess("Vous êtes connecté en tant que : " + user.getAffichageMinimal());
    }

    public void deconnexion() {
        user = new Utilisateur();
        PromptUtils.printSuccess("Vous êtes déconnecté");
    }

    public ArrayList<Produit> rechercherParMotClef(){
        String recherche = IHM.getUserIn(PromptUtils.yel("Entrer un mot-clef : "));
        ArrayList<Produit> resultats = new ArrayList<Produit>();
        for (MotClef mot : this.motClef) {
            if (mot.compare(recherche)) {
                List<Produit> temp = mot.getProduits();
                for (Produit p : temp) {
                    resultats.add(p);
                }
            }
        }
        return resultats;
    }

    public String[] getMenuCateg(List<Categorie> categs) {
        String[] res = new String[categs.size()];
        for (int i = 0; i < categs.size(); i++) {
            res[i] = categs.get(i).getNom();
        }
        return res;
    }

    public List<Produit> rechercherParCategorie() {
        String choixCateg = IHM.deroulerMenu(PromptUtils.yel("Selectionnez une categorie :"), getMenuCateg(categories));
        Categorie res = null;
        for (Categorie categ : categories) {
            if (categ.getNom() == choixCateg) {
                res = categ;
                break;
            }
        }
        API.fetchSousCategorie(res);
        List<Categorie> sousCateg = res.getSousCategories();
        if (res.getSousCategories() != null) {
            String choixSousCateg = IHM.deroulerMenu(PromptUtils.yel("Selectionnez une sous-categorie :"), getMenuCateg(sousCateg));
            for (Categorie categ : sousCateg) {
                if (categ.getNom() == choixSousCateg) {
                    res = categ;
                    break;
                }
            }
        }
        API.fetchProductByCategorie(res);
        return res.getProduits();
    }

    public void rechercher() {
        // selection du type de recherche
        String[] menu = user.getMenuRecherche();
        String choix = IHM.deroulerMenu(PromptUtils.yel("Selectionnez un type de recherche :"), menu);
        List<Produit> res = null;
        switch (choix) {
            case "Rechercher par mot clef":
                API.fetchMotClef(this);
                res = rechercherParMotClef();
                break;
            case "Rechercher par catégorie":
                API.fetchCategories(this);
                res = rechercherParCategorie();
                break;
            default:
                return;
        }
    }

    public void acheterObjetEnchere() {
        System.out.println("acheter objet enchère");
    }

    public void acheterUnObjet() {
        System.out.println("acheter un objet");
    }

    public void evaluerUnUtilisateur() {
        System.out.println("evaluer un utilisateur");
    }

    public void inscriptionAcheteur() {
        String[] formulaire = Acheteur.getFormulaireInscription();
        String[] parametres = IHM.remplirFormulaire(PromptUtils.b("Formulaire d'inscription (acheteur)"), formulaire);
        // on connecte l'acheteur automatiquement
        Acheteur user = new Acheteur(parametres);
        users.add(user);
        API.addAcheteur( user);
        PromptUtils.printSuccess("Vous êtes connecté en tant que:\n  " + user.getAffichageMinimal());
    }

    public void inscriptionVendeur() {
        // remplir les données du vendeur
        String[] formulaire = Vendeur.getFormulaireInscription();
        String[] parametres = IHM.remplirFormulaire(PromptUtils.b("Formulaire d'inscription (vendeur)"), formulaire);

        // remplir les données bancaires
        String[] menuTypeDonnees = { "RIB", "CB" };
        String typeDonnees = IHM.deroulerMenu(
                PromptUtils.yel("Choisissez un type de données bancaires pour la vérification de vos données"), menuTypeDonnees);
        String[] donneesBancaires = DonneesBancaires.getFormulaire(typeDonnees);
        String[] donneesRemplies = IHM.remplirFormulaire(PromptUtils.yel("Entrez vos données bancaires :"), donneesBancaires);

        // vérification : si ce ne sont pas les mêmes noms et prénoms, échec, sinon les
        // Données et le Vendeur sont créés
        boolean donneesOK = DonneesBancaires.verifierVendeur(formulaire, donneesBancaires);
        if (!donneesOK) {
            PromptUtils.printError("Echec : les données bancaires ne correspondent pas au vendeur");
            return;
        }

        // création des objets
        DonneesBancaires dataBank = null;
        if (typeDonnees == "RIB")
            dataBank = new RIB(donneesRemplies);
        else
            dataBank = new CarteBancaire(donneesRemplies);
        Vendeur vendeur = new Vendeur(parametres, dataBank);

        // connexion de l'utilisateur
        user = vendeur;
        users.add((Inscrit) user);
        API.addVendeur((Vendeur) user);

        PromptUtils.printSuccess("Données bancaires correctes : " + dataBank.toString());
        PromptUtils.printSuccess("Vous êtes connecté en tant que :\n  " + user.getAffichageMinimal());
    }
}
