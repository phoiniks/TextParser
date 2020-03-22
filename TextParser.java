import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import java.lang.Thread;


class TextParser
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
	String[] capital = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
			  "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			  "U", "V", "W", "X", "Y", "Z", "Ä", "Ö", "Ü"};

	Logger LOGGER = Logger.getLogger( TextParser.class.getName() );
    String eingabeDatei = args[0];
	String lexikonDatei = args[0].replaceAll(".txt", "") + ".yml";
	File datei = new File(lexikonDatei);
	BufferedWriter lexikonSchreiber = new BufferedWriter(new FileWriter(lexikonDatei));
	BufferedReader br = new BufferedReader(new FileReader(eingabeDatei));	
	Hashtable<String, String> lexikon = new Hashtable<String, String>();
    Hashtable<String, String> satzanfaenge = new Hashtable<String, String>();
	FileHandler fh = new FileHandler(args[0] + ".log");
	fh.setFormatter(new SimpleFormatter());
	LOGGER.addHandler(fh);
	int zaehler = 0;
	String line = "";
	String satz    = null;
	Scanner saetze = null;

	
	try{
	    saetze = new Scanner(new File(eingabeDatei)).useDelimiter("\\.\\n");
	} catch(FileNotFoundException e) {
	    System.out.println(e);
	}
	
	zaehler = 0;
	while(saetze.hasNext()) {
	    satz   = null;	    
	    satz   = saetze.next();	    
	    
	    String[] woerter = satz.split(" ");
	    System.out.println(woerter.length);
	    System.out.println("Satzanfang: " + woerter[0]);
	    System.out.println(satz);

	    satzanfaenge.put(woerter[0], "Satzanfang");
	    zaehler++;
        lexikonSchreiber.write(zaehler + ": " + woerter[0]);        
	}


    for(String anfang : satzanfaenge.keySet()) {
        System.out.println(anfang);
        LOGGER.info(anfang);
    }

	
	while(line != null){
	    try
		{
		    line = br.readLine();
		    line = line.replaceAll("[,.:!?]", "");
		    String[] array = line.split(" ");

		    for(int i = 0; i < array.length; i++)
                {
                    if(array[i].length() > 0 && !satzanfaenge.contains(array[i])) {
                        try{
                            incrementHashValue(lexikon, array[i]);
                        }catch(NumberFormatException e){
                            LOGGER.info(array[i]);
                            continue;
                        }
                    }
                }
		}
	    catch(NullPointerException npe)
		{
		    break;
		}
	}


	List<String> liste = new ArrayList<String>();
	liste.addAll(lexikon.keySet());
	List<String> lemmata = liste.stream().sorted().collect(Collectors.toList());

	zaehler = 0;
	int     zaehlerGetaggt = 0;
	boolean getaggt = false;
	for(String key : lemmata){
	    String value = lexikon.get(key);

	    String zeile = "lemma: " + key + "\n  " + "frequenz: " + value + "";
	    
	    try
		{
		    for(String str : capital)
			{
			    if(key.startsWith(str)) {
                    zeile += "\n  Status: möglicherweise Substantiv";
                    LOGGER.info(str);
                    getaggt = true;
			    }
			}
		}
	    catch (NullPointerException npe)
		{
		    System.out.println(npe);
		    break;
		}

	    if(key.contains("ge") && key.endsWith("t")) {
	       zeile += "\n  Wortart: möglicherweise Partizip";
	       getaggt = true;
	    }
	    else if(key.contains("ig") && (key.length() - key.indexOf("ig") < 5)) {
		zeile += "\n  Wortart: möglicherweise Adjektiv oder substantiviertes Adjektiv";
		getaggt = true;
	    }
	    else if(key.endsWith("iert")) {
		zeile += "\n  Form: möglicherweise verbal";
		getaggt = true;
	    }
	    
	    lexikonSchreiber.write(zeile + "\n");
	    lexikonSchreiber.flush();

	    zaehler++;

	    if(getaggt)
		zaehlerGetaggt++;

        if( key != "-")
            LOGGER.info("Lemma: " + key + ", getaggt: " + getaggt);

	    getaggt = false;
	}

	
	System.out.println("Distinkte Lemmata: " + zaehler);
	System.out.println("Davon getaggt:     " + zaehlerGetaggt);
	System.out.println("Differenz ungetaggt / getaggt: " + (zaehler - zaehlerGetaggt));

    LOGGER.info("Lexikon in: " + args[0] + ".yml");
    
	lexikonSchreiber.close();	
	br.close();
    }


    public static<K> void incrementHashValue(Hashtable<K, String> dictionary, K key){
	dictionary.putIfAbsent(key, "0");
	dictionary.put(key, String.valueOf(Integer.parseInt(dictionary.get(key)) + 1));
    }
}
