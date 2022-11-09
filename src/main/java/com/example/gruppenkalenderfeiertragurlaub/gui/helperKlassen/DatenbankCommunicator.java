package com.example.gruppenkalenderfeiertragurlaub.gui.helperKlassen;

import com.example.gruppenkalenderfeiertragurlaub.speicherklassen.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class DatenbankCommunicator {

    private static Connection conn;
    private static String url = "jdbc:mariadb://localhost/verpflegungsgeld";
    private static String user = "root";
    private static String password = "";


    /**
     * Establishes a conection to the database with the information specified in the url, user and password global variables
     * @return if conection was succesfully established, returns true, otherwise false
     */
    public static boolean establishConnection() {
        //create connection for a server installed in localhost, with a user "root" with no password
        try {
            conn = DriverManager.getConnection(url, user, password);

            return true;

        } catch (SQLException e) {
            System.out.println("Database not found. Please make sure the correct Database is available");
            return false;
        }
    }

    /**
     * Liest alle werte aus der Tabelle kuechenplanung für das übergebene Jahr und speichert diese in eine ArrayList von KüchenkalenderTag objekten
     * @param jahr
     * @return Liste aller datenbankeinträge in kuechenplanung für das übergebene jahr als
     * @throws SQLException
     */
    public static ArrayList<KuechenKalenderTag> readKuechenKalenderTage(Integer jahr) throws SQLException {
        ArrayList<KuechenKalenderTag> kuechenKalenderTagListe = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            try(ResultSet rs = stmt.executeQuery("SELECT * FROM kuechenplanung WHERE kuechenplanung.datum >= '"
                    + jahr + "-01-01' AND kuechenplanung.datum <= '" + jahr + "-12-31'")) {
                while(rs.next()) {
                    LocalDate datum = LocalDate.parse(rs.getDate("datum").toString());

                    //TODO change columnLabel below to new and corrected Colum name
                    Boolean kuecheOffen = rs.getBoolean("gooeffnet");
                    KuechenKalenderTag kuechenTag = new KuechenKalenderTag(datum, kuecheOffen);
                    kuechenKalenderTagListe.add(kuechenTag);
                }
            }
        }
        return kuechenKalenderTagListe;
    }

    /**
     * Liest für das übergebene Jahr alle einträge in der Spalte datum der kuechenplanung tabelle. Für Jedes Gelesene Datum sucht es in der Tabelle
     * betriebsurlaub nach einem identischen datum. Wenn ein datum gefunden wird ist klar das es sich bie dem gelesenen Datum um einen bereits festgelgten
     * Betriebsurlaubstag handelt, (ein objekt Betriebsurlaubstag wird erstellt mit dem Booleanwert istBetriebsurlaubstag auf true)
     * wird kein entsprechendes Datum gefunden so ist der Tag kein betriebsurlaubstag (Betriebsulraubstag wird mit Boolean wert auf False erstellt)
     * die Estellten Betriebsurlaubstagsobjekte werden in einer Arraylist zurückgegeben.
     * @param jahr
     * @return Liste aller datenbankeinträge datum aus kuechenplanung und betriebsurlaub für das übergebene jahr
     * @throws SQLException
     */
    public static ArrayList<BetriebsurlaubsTag> readBetriebsurlaubTage(Integer jahr) throws SQLException {
        ArrayList<BetriebsurlaubsTag> betriebsurlaubsTagListe = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            try(ResultSet rs = stmt.executeQuery("select\n" +
                    "\tk.datum as 'normalDatum',\n" +
                    "\tb.datum as 'betiebsurlaubsDatum'\n" +
                    "FROM \n" +
                    "\tkuechenplanung k left join betriebsurlaub b on k.datum = b.datum\n" +
                    "\tWHERE k.datum >= '" + jahr + "-01-01' AND k.datum <= '" + jahr + "-12-31'")) {
                while(rs.next()) {
                    LocalDate datum = LocalDate.parse(rs.getDate("normalDatum").toString());
                    boolean isBetiebsurlaub = false;
                    if(rs.getDate("betiebsurlaubsDatum") != null) {
                        isBetiebsurlaub = true;
                    }
                    BetriebsurlaubsTag betriebsurlaub = new BetriebsurlaubsTag(datum, isBetiebsurlaub);
                    betriebsurlaubsTagListe.add(betriebsurlaub);

                }
            }
        }
        return betriebsurlaubsTagListe;
    }

    /**
     * Liest alle Einträge für das gegebene Jahr und die gegebene Gruppe bzw Gruppenfamilie
     * aus der Datenbanktabelle Gruppenkalender und speichert diese in einer ArrayListe von
     * GruppenKalenderTag objekten.
     * @param jahr
     * @return Liste aller datenbankeinträge in gruppenkalender für das übergebene jahr
     * @throws SQLException
     */
    public static ArrayList<GruppenKalenderTag> readGruppenKalenderTage(Integer jahr, Object gruppeOderFamilie) throws SQLException {
        String gruppeOderFamilieSelectedBedinung = " AND ";
        if(gruppeOderFamilie.getClass() == GruppeFuerKalender.class) {
            gruppeOderFamilieSelectedBedinung = gruppeOderFamilieSelectedBedinung + "gruppenkalender.gruppe_id = "
                    + ((GruppeFuerKalender) gruppeOderFamilie).getGruppeId();
            generateTageIfMissing((GruppeFuerKalender) gruppeOderFamilie, jahr);
        } else {
            boolean isFirstGruppInArray = true;
            gruppeOderFamilieSelectedBedinung = gruppeOderFamilieSelectedBedinung + "(";
            for (GruppeFuerKalender gr : ((GruppenFamilieFuerKalender)gruppeOderFamilie).getGruppenDerFamilie() ) {
                if(!isFirstGruppInArray) {
                    gruppeOderFamilieSelectedBedinung = gruppeOderFamilieSelectedBedinung + " OR ";
                }
                gruppeOderFamilieSelectedBedinung = gruppeOderFamilieSelectedBedinung + "gruppenkalender.gruppe_id = "
                        + ((GruppeFuerKalender) gr).getGruppeId();
                generateTageIfMissing(gr, jahr);
                isFirstGruppInArray = false;
            }
            gruppeOderFamilieSelectedBedinung = gruppeOderFamilieSelectedBedinung + ")";
        }

        ArrayList<GruppenKalenderTag> kalenderTagListe = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            try(ResultSet rs = stmt.executeQuery("SELECT * FROM gruppenkalender WHERE gruppenkalender.datum >= '"
                    + jahr + "-01-01' AND gruppenkalender.datum <= '" + jahr + "-12-31'" + gruppeOderFamilieSelectedBedinung)) {
                while(rs.next()) {
                    LocalDate datum = LocalDate.parse(rs.getDate("datum").toString());
                    Boolean kuecheOffen = rs.getBoolean("essensangebot");
                    Integer gruppen_id =  rs.getInt("gruppe_id");
                    Character gruppenstatus = rs.getString("gruppenstatus").toCharArray()[0];
                    kalenderTagListe.add(new GruppenKalenderTag(gruppen_id, datum, gruppenstatus, kuecheOffen));
                }
            }
        }

        return kalenderTagListe;
    }
    //TODO write Documentation
    public static ArrayList<GruppenFamilieFuerKalender> getAllGruppenFamilienUndGruppen() throws SQLException {
        ArrayList<GruppenFamilieFuerKalender> gruppenFamilieListe = new ArrayList<>();


        try(Statement stmt = conn.createStatement()) {
            try(ResultSet rs = stmt.executeQuery("select f.id as 'familienId', f.name as 'familienName', g.id 'gruppeId', g.name as 'gruppeName' \n" +
                    "from gruppenfamilie f inner join gruppe g on f.id = g.gruppenfamilie_id;")){
                while (rs.next()){
                    Integer familienId = rs.getInt("familienId");
                    String familienName = rs.getString("familienName");
                    Integer gruppeId = rs.getInt("gruppeId");
                    String gruppeName = rs.getString("gruppeName");

                    GruppeFuerKalender neueGruppe = new GruppeFuerKalender(gruppeId, gruppeName, familienId);

                    Boolean gruppeExists = false;
                    for (GruppenFamilieFuerKalender familie : gruppenFamilieListe) {
                        if(familie.getFamilieId() == familienId) {
                            gruppeExists = true;
                            familie.getGruppenDerFamilie().add(neueGruppe);
                            break;
                        }
                    }
                    if(!gruppeExists) {
                        GruppenFamilieFuerKalender neueFamilie = new GruppenFamilieFuerKalender(familienId, familienName, new ArrayList<>());
                        neueFamilie.getGruppenDerFamilie().add(neueGruppe);
                        gruppenFamilieListe.add(neueFamilie);
                    }
                }
            }
        }

        return gruppenFamilieListe;
    }
    static boolean generateTageIfMissing(GruppeFuerKalender gr, Integer jahr) throws SQLException {

        Boolean tageNeedToBeGenerated = checkIfTageNeedToBegenerated(gr, jahr);

        if(tageNeedToBeGenerated) {
            try(Statement stmt = conn.createStatement()) {
                //TODO create code to generate all work days for given year and insert them into the datbase;
            }
        }

        return tageNeedToBeGenerated;

    }

    /**
     * Überprüft ob der Erste Werktag des Gegebnen jahres für die gegebne Gruppe als Eintrag in der Datenbank Existiert.
     * (Das Programm soll nur ganze jahre für eine gruppe gleichzeitig generieren, und nur werktage. Deswegen reicht es aus
     * nach dem Ersten Werktag des Jahres zu suchen um festzustellen ob daten für das gegebene Jahr in der Datenbank vorhanden sind
     * @param gr
     * @param jahr
     * @return true wenn der Tag in der Datenbank vorhanden ist, False wenn er nicht vorhanden ist
     * @throws SQLException
     */
    private static Boolean checkIfTageNeedToBegenerated(GruppeFuerKalender gr, Integer jahr) throws SQLException {
        //TODO Implement method to find the first Werktag of a year and check for it instead of for 01-01-20YY
        try (Statement stmt = conn.createStatement()) {
            try(ResultSet rs = stmt.executeQuery("SELECT EXISTS (SELECT * FROM gruppenkalender g WHERE g.datum = '" + jahr +
                    "-01-01' AND g.gruppe_id = " + gr.getGruppeId() +") as dayExists;")) {
                System.out.println("got this far");
                rs.next();
                return rs.getBoolean("dayExists");
            }
        }
    }
}
