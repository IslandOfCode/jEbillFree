//
// Questo file � stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.8-b130911.1802 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andr� persa durante la ricompilazione dello schema di origine. 
// Generato il: 2018.10.22 alle 05:14:10 PM CEST 
//


package it.islandofcode.jebill.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per RitenutaType.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * <p>
 * <pre>
 * &lt;simpleType name="RitenutaType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;length value="2"/>
 *     &lt;enumeration value="SI"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "RitenutaType", namespace = "http://ivaservizi.agenziaentrate.gov.it/docs/xsd/fatture/v1.2")
@XmlEnum
public enum RitenutaType {


    /**
     * 
     * 						SI = Cessione / Prestazione soggetta a ritenuta
     * 					
     * 
     */
    SI;

    public String value() {
        return name();
    }

    public static RitenutaType fromValue(String v) {
        return valueOf(v);
    }

}
