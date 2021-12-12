package BDD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import System.*;

public class API {
    private static Connection con;

    public static void setConnexion() throws Exception {
        try {
            con = connexion();
        } catch (Exception e) {
            throw e;
        }
    }

    public static Connection connexion() throws Exception {
        Connection con = null;
        try {
            con = DriverManager.getConnection(
                    "jdbc:mysql://db4free.net:3306/i_buy_su?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false",
                    "ibuysubdd", "ibuysubdd");
            System.out.println("Connexion avec la base de données établie");
        } catch (SQLException e) {
            throw e;
        }
        return con;
    }

    public static void addAcheteur(Acheteur a) {
        String requete = "INSERT into Acheteur (ID, nom, prenom, pseudo, numeroTel, mail, motdepasse, adresse) VALUES(";
        requete += a.getId() + ",";
        requete += "'" + a.getNom() + "',";
        requete += "'" + a.getPrenom() + "',";
        requete += "'" + a.getPseudo() + "',";
        requete += a.getNumeroTel() + ",";
        requete += "'" + a.getMail() + "',";
        requete += "'" + a.getMotdepasse() + "',";
        requete += "'" + a.getAdresse().toString() + "')";
        try {
            if (con == null) {
                con = connexion();
            }
            Statement stmt = con.createStatement();
            stmt.executeUpdate(requete);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addMotClef(MotClef mot, Produit p) {
        try {
            if (con == null) {
                con = connexion();
            }
            Statement stmt = con.createStatement();
            stmt.executeUpdate("Insert into Motcle Values ('" + mot.getNom() + "','" + p.getId() + "')");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addProduit(IBuySu system, Produit p) {
        String requete = "";
        if(p instanceof Enchere) {
            requete = "INSERT into Enchere (id, titre, description, prix_de_depart, estVendu, estRecu, vendeur, categorie, photo) VALUES(";
        } else {
            requete = "INSERT into Vente_directe (id, titre, description, prix_de_depart, estVendu, estRecu, vendeur, categorie, photo) VALUES(";
        }
        requete += p.getId() + ",";
        requete += "'" + p.getTitre() + "',";
        requete += "'" + p.getDescription() + "',";
        requete += p.getPrix_de_depart() + ",";
        requete += p.getEstVendu() + ",";
        requete += p.getEstRecu() + ",";
        requete += p.getVendeur().getId() + ",";
        requete += "'" + p.getCategorie().getNom() + "',";
        requete += "'" + p.getPhoto() + "')";
        try {
            if (con == null) {
                con = connexion();
            }
            Statement stmt = con.createStatement();
            stmt.executeUpdate(requete);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void fetchUsers(IBuySu system){
        try{
            List<Inscrit> users = new ArrayList<Inscrit>();
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery("select * from Acheteur");
            while(res.next()){
                users.add(new Acheteur(res.getInt("id"), res.getString("pseudo"), res.getString("nom"), res.getString("prenom"), res.getInt("numeroTel"), res.getString("mail"),  res.getString("motdepasse"), -1, null, -1,null, null));
            }
            Statement stmt1 = con.createStatement();
            res = stmt1.executeQuery("select * from Vendeur");
            while(res.next()){
                users.add(new Vendeur(res.getInt("id"), res.getString("pseudo"), res.getString("nom"), res.getString("prenom"), res.getInt("numeroTel"), res.getString("mail"),  res.getString("motdepasse"), -1, null, -1,null,null, null));
            }
            system.setUsers(users);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void fetchCategories(IBuySu system){
        try{
            ArrayList<Categorie> cate = new ArrayList<Categorie>();
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery("select * from Categorie where parent_categorie is null");
            while(res.next()){
                cate.add(new Categorie(res.getString("nom")));
            }
            system.setCategories(cate);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void fetchSousCategorie(Categorie cat){
        try{
            List<Categorie> sub = new ArrayList<Categorie>();
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery("select * from Categorie where parent_categorie='" + cat.getNom() + "'");
            while(res.next()){
                sub.add(new Categorie(res.getString("nom")));
            }
            cat.setSousCategories(sub);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void fetchProductByCategorie(IBuySu system, Categorie cat){
        try{
            List<Produit> prod = new ArrayList<Produit>();
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery("select * from Enchere where categorie='"+cat.getNom() + "'");
            while(res.next()){
                byte isSold = res.getByte("estVendu");
                byte isReceived = res.getByte("estRecu");
                prod.add(new Enchere(res.getInt("duree_enchere"),res.getString("titre"),res.getString("description"), (Vendeur) system.getUserById(res.getInt("vendeur")), res.getString("photo"), res.getInt("prix_de_depart"), cat, isSold!=0, isReceived!=0));
            }
            Statement stmt1 = con.createStatement();
            res = stmt1.executeQuery("select * from Vente_directe where categorie='"+cat.getNom()+"'");
            while(res.next()){
                prod.add(new Produit(res.getString("titre"),res.getString("description"), (Vendeur) system.getUserById(res.getInt("vendeur")), res.getString("photo"), res.getInt("prix_de_depart"), cat));
            }
            cat.setProduits(prod);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void fetchProductByMotcle(MotClef motcle){
        try{
            List<Produit> prod = new ArrayList<Produit>();
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery("select * from Enchere e,Motcle mp where mp.nom='"+motcle.getNom()+"' and mp.produit=e.id");
            while(res.next()){
                byte isSold = res.getByte("estVendu");
                byte isReceived = res.getByte("estRecu");
                prod.add(new Enchere(res.getInt("duree_enchere"),res.getString("titre"),res.getString("description"), null, res.getString("photo"), res.getInt("prix_de_depart"), new Categorie(res.getString("categorie")), isSold!=0, isReceived!=0));
            }
            Statement stmt1 = con.createStatement();
            res = stmt1.executeQuery("select * from Vente_directe vd,Motcle mp where mp.nom='"+motcle.getNom()+"' and mp.produit=vd.id");
            while(res.next()){
                prod.add(new Produit(res.getInt("id"), res.getString("titre"),res.getString("description"), null, res.getString("photo"), res.getInt("prix_de_depart"), new Categorie(res.getString("categorie"))));
            }
            motcle.setProduits(prod);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void fetchMotClef(IBuySu system){
        try{
            List<MotClef> motcles = new ArrayList<MotClef>();
            Statement stmt = con.createStatement();
            ResultSet res = stmt.executeQuery("select * from Motcle");
            while(res.next()){
                MotClef mot = new MotClef(res.getString("nom"),null);
                motcles.add(mot);
            }
            system.setMotClef(motcles);;
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void addVendeur(Vendeur v) {
        String requete = "INSERT into Vendeur (ID, nom, prenom, pseudo, numeroTel, mail, motdepasse, adresse, donnees_banquaires) VALUES(";
        requete += v.getId() + ",";
        requete += "'" + v.getNom() + "',";
        requete += "'" + v.getPrenom() + "',";
        requete += "'" + v.getPseudo() + "',";
        requete += v.getNumeroTel() + ",";
        requete += "'" + v.getMail() + "',";
        requete += "'" + v.getMotdepasse() + "',";
        requete += "'" + v.getAdresse().toString() + "',";
        requete += "'" + v.getDonneesBanque().toString() + "')";
        try {
            if (con == null) {
                con = connexion();
            }
            Statement stmt = con.createStatement();
            stmt.executeUpdate(requete);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int fetchNbProduct() {
        try {
            if (con == null) {
                con = connexion();
            }
            Statement stmt = con.createStatement();
            ResultSet  enchere = stmt.executeQuery("SELECT COUNT(*) FROM Enchere");
            enchere.next();
            int nbEnchere = enchere.getInt("COUNT(*)");
            ResultSet directe = stmt.executeQuery("SELECT COUNT(*) FROM Vente_directe");
            directe.next();
            int nbDirecte = directe.getInt("COUNT(*)");
            return nbDirecte + nbEnchere;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int getNbInscrit() {
        try {
            if (con == null) {
                con = connexion();
            }
            Statement stmt = con.createStatement();
            ResultSet vendeurs = stmt.executeQuery("SELECT COUNT(*) FROM Vendeur");
            vendeurs.next();
            int nbVendeurs = vendeurs.getInt("COUNT(*)");
            ResultSet acheteurs = stmt.executeQuery("SELECT COUNT(*) FROM Acheteur");
            acheteurs.next();
            int nbAcheteurs = acheteurs.getInt("COUNT(*)");
            return nbAcheteurs + nbVendeurs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
