# jEBill

Un avanzato estrattore/generatore di allegati da fatture elettroniche.

## Primo avvio

Il programma è _standalone_, cioè non ha bisogno di essere installato. Essendo scritto in Java, è necessario che
una JRE (anche JDK) sia presente sulla macchina.

### Prerequisiti

Se non è già presente, è necessario scaricare l'ambiente java.

Sul sito:
[java.com/download](https://www.java.com/it/download/)
è possibile scaricare la versione più aggiornata.

**Attenzione!** jEBill necessita di una versione di java pari o superiore alla 1.8 per poter essere eseguito.
Anche se avete una versione compatibile, è sempre consigliabile scaricare l'ultimo _update_ disponibile.


### Download e installazione

Raggiungere l'indirizzo:
[https://github.com/IslandOfCode/jEbillFree/releases](https://github.com/IslandOfCode/jEbillFree/releases)
Da qui potete avviare il download dell'ultima versione disponibile di jEBill.

**Attenzione!** Questa procedura sarà soggetta a modifiche future. Inoltre, il programma è dotato di funzione
di ricerca di aggiornamento integrato, quindi è possibile ottenere direttamente il link alla versione più aggiornata.

Una volta scaricato il pacchetto zip, decomprimerlo in una cartella dedicata.

Questo passaggio è fondamentale, perchè jEBill tende a creare una serie di file di supporto fin dal suo primo avvio.
Porlo in una cartella dedicata, permette di mantenere in ordine il proprio sistema.

### Avvio

Per avviare jEBill, è sufficente fare doppio click sul file eseguibile.
Dopo aver visualizzato brevemente un'immagine, necessaria per nascondere il tempo di attesa
per il caricamento dell'applicativo, apparirà la finestra principale del programma.

## Manutenzione e aggiornamento

Come già accennato, il programma tende a creare un quantitativo di file di supporto, tra cui
troviamo:

- **CA.pem** il file che contiene tutti i certificati, necessario per la verifica della firma digitale.
- **jebill_gg-mm-hh.mm.ss.log** file di log con timestamp. Viene creato ad ogni avvio di jEBill.
- **jebill.conf** file di configurazione.
- **openssl.exe** la presenza è limitata al ciclo di esecuzione di jEBill. Quando l'applicazione viene chiusa,
  il file viene automaticamente cancellato. Viene inoltre estratto solo quando viene esplicitamente richiesta
  la verifica della firma digitale.

In base a quante volte utilizzerete jEBill, il numero di file di log potrebbe crescere e diventare insostenibile.
Questi file possono essere cancellati senza problemi (assicurandosi che l'applicativo sia chiuso).

Gli altri file pure possono essere rimossi (jebill.conf viene ricreato se non esiste, CA.pem può essere scaricato
in ogni momento), ma con un'unica accortezza: dato che jebill.conf contiene dei valori relativi a CA.pem, questi
andrebbero cancellati entrambi.
Se per caso ne venisse cancellato solo uno, non sarebbe un problema, dato che l'applicativo provvederebbe in autonomia
a ricreare l'altro.

**Attenzione!** Nel caso in cui openssl.exe sia presente, ma non ci sono istanze di jEBill in esecuzione, è possibile
che abbiate un'instanza del programma aperta ma nascosta oppure che l'esecuzione precedente non è terminata correttamente.
Cercate di capire se jEBill sia ancora aperto: se si, chiudetelo e verificate che il file venga cancellato. Se no, o il file
non viene cancellato, potete anche provvedere voi stessi.
In realtà non c'è effettivamente bisogno di rimuoverlo, dato che questo file viene estratto ogni volta che viene richiesta
la verifica delle firma e quindi, se fosse ancora presente, verrebbe sovrascritto.


Nel caso in cui si aggiorni l'applicativo con una nuova versione, è consigliabile rimuovere tutti i file sopra elencati,
sovrascrivere l'eseguibile con la nuova versione e poi eseguire una prima volta jEBill. In questo modo si evitano incompatibilità
che si possono venire a creare se esistono differenze nei formati dei file tra le varie versioni.

## Autore

**Pier Riccardo Monzo** - *One man team* - [IslandOfCode.it](https://www.islandofcode.it/)

## Licenza
Questo programma è sviluppato sotto licenza **GPLv3**.
Fate riferimento al file [LICENSE.md](LICENSE.md), per avere sempre la versione più aggiornata.