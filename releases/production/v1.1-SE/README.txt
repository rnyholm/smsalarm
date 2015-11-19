Sms Alarm README
================

Sms Alarm Copyright© 2011-2012 av Robert Nyholm. Alla rättigheter reserverade.
Se licens och användarvillkor längre ner.

Allmäna ändringingar från version 0.9.3 till 1.1
========================================
- Möjlighet att ställa in två nummer som Sms Alarm larmar på, primär och sekundär larm.
- Olika eller samma larmsignaler på primär och sekundär larmet.
- Gränssnittet har utökats.
- Hanteringen av datumstämpeln i larmen har optimerats.
- Ändrat metoden som Sms Alarm spelar upp larm signal på, dvs. nu spelar Sms Alarm upp
  larmsignalen med ringvolymens styrka om man väljer att applikationen ska uttnyttja 
  telefonens ljudinställningar. Annars spelar Sms Alarm upp larmsignalen med full volym.
- Lagt till flera sökarljud, nu finns totalt 33 stycken.  
- Sms Alarms prioritet har ökats, har funnits vissa komplikationer då användare använt 
  både Sms Alarm och andra tredje parts meddelande program.
  
Ändringar specifika för version 1.1-SE
======================================
- Ytterligare utökat gränssnitt.
- Möjlighet att kvittera larm till angivet telefonnumer.

Kort användaranvisning för version 1.1
======================================
För att applikationen ska fungera och reagera på sms måste åtminstone ett nummer anges,
antingen primärlarm, sekundärlarm eller båda. Dock kan inte primärlarm och sekundärlarm 
ha samma nummer. 
Larmsignal anges för respektive larm genom att först välja vilket larm man vill ange larmsignal
för i dropdown menyn. Sen väljer man bara signal genom att trycka på "Ändra signal", det går 
också att lyssna på signalen.
När man anger att man vill använda sig av telefonens ljudinställningar så tar applikationen 
hänsyn till ringsingnalens ljudstyrka och justerar larmsignalen därefter. Hänsyn tas också till
om telefonen är i vibrationsläge eller tyst. Om man inte använder telefonens ljudinställningar 
kommer Sms Alarm att larma med full volym samt vibrera vid larm, ÄVEN FAST TELEFONEN ÄR I LJUDLÖST 
LÄGE.
Vid inkommande larm visas en notifikation, innehållande rubriken(PRIMÄR LARM! eller SEKUNDÄR LARM!) samt
en kort visning av larmet. Om man trycker på notifikationen så kommer man till meddelande inkorgen.
Inställningar som angetts sparas, dvs. om du startar om telefonen så finns alla inställningar kvar.

Kort användaranvisning för version 1.1-SE
=========================================
För att kunna kvittera på larm behöver man först aktivera det i gränssnittet, samt ange nummer som
applikationen ska kvittera till. Det är mycket VIKTIGT att ange nummer annars kommer man INTE kunna
kvittera larm. Kvitteringen fungerar endast på primärlarmet. Applikationen måste kvittera mot ett gsm
nummer, det har med riktnummer att göra.
Om kvitteringen är aktiverad och man får primärlarm, så visas inte notifikationen, istället kommer ett
helt nytt fönster upp på telefonen där det står att Jomala FBK har larm, samt fulla larmmeddelandet och 
två knappar, en för kvittering och en som avbryter. Vid kvittering så ringer telefonen upp förinställda,
användaren måste själv lägga på. Om man avbryter kvitteringen går telefonen tillbaka till den punkt den
var innan larmet kom.
Om man får ett sekundär larm, visas notifikationen och det förfarandet fungerar som vanligt. Det samma 
gäller primär larmet om kvittering är inaktiverat.

OBS
===
Alla egenskaper som Sms Alarm v1.1 har och alla användaranvisningar för v1.1 har/gäller också för
SmsAlarm v1.1-SE.

Licens och användning
=====================
Med applikation menas alla versioner av Sms Alarm, och alla filer som tillhör applikationen, 
inklusive dokumentation.

Frånsägelser
------------
Utvecklaren kan inte hållas ansvarig för eventuella komplikationer som uppstår med din enhet, eller 
användandet av applikationen. Utvecklaren fråntar sig även ansvar för komplikationer som uppstått vid
indirekt användande av applikationen.

Användarrättigheter
-------------------
Den version av Sms Alarm som DU har tillgång till är Sms Alarm v1.1-SE, dvs. en version av Sms Alarm
som innehåller funktionalitet som endast är avsedd för Jomala FBK. Detta betyder att DU som användare
av denna applikation inte för sprida vidare, applikationen till tredje part som inte är medlem i 
Jomala FBK. 
En allmän version Sms Alarm v1.1 finns på Google Market för nedladdning.


Robert Nyholm
robert.nyholm@aland.net