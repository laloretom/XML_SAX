import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class insurancePromedio extends DefaultHandler {
    private static final String CLASS_NAME = insurancePromedio.class.getName();
    private final static Logger LOG = Logger.getLogger(CLASS_NAME);

    private SAXParser parser = null;
    private SAXParserFactory spf;

    private double totalSales;
    private boolean inSales;

    private String currentElement;
    private String id;
    private String name;
    private String lastName;
    private String phone;
    private String car;
    private String model;
    private String insurance;
    private String contract;

    private String keyword;

    private HashMap<String, Double> subtotales;
    private HashMap<String, Integer> contadorMap;

    public insurancePromedio() {
        super();
        spf = SAXParserFactory.newInstance();
        // verificar espacios de nombre
        spf.setNamespaceAware(true);
        // validar que el documento este bien formado (well formed)
        spf.setValidating(true);

        subtotales = new HashMap<>();
        contadorMap = new HashMap<>();
    }

    private void process(File file) {
        try {
            // obtener un parser para verificar el documento
            parser = spf.newSAXParser();

        } catch (SAXException | ParserConfigurationException e) {
            LOG.severe(e.getMessage());
            System.exit(1);
        }
        System.out.println("\nStarting parsing of " + file + "\n");
        try {
            // iniciar analisis del documento
            keyword = car;
            parser.parse(file, this);
        } catch (IOException | SAXException e) {
            LOG.severe(e.getMessage());
        }
    }

    @Override
    public void startDocument() throws SAXException {
        // al inicio del documento inicializar
        // las ventas totales
        totalSales = 0.0;
    }

    @Override
    public void endDocument() throws SAXException {
        // Se proceso todo el documento, imprimir resultado
        Set<Map.Entry<String,Double>> entries = subtotales.entrySet();
        for (Map.Entry<String,Double> entry: entries) {
            double promedio = entry.getValue() / contadorMap.get(entry.getKey());
            System.out.printf("%-15.15s $%,9.2f\n",entry.getKey(),promedio);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {

        if (localName.equals("insurance_record")) {
            inSales = true;
        }
        currentElement = localName;
    }

    @Override
    public void characters(char[] bytes, int start, int length) throws SAXException {

        switch (currentElement) {
            case "id":
                this.id = new String(bytes, start, length);
                break;
            case "first_name":
                this.name = new String(bytes, start, length);
                break;
            case "last_name":
                this.lastName = new String(bytes, start, length);
                break;
            case "phone":
                this.phone = new String(bytes, start, length);
                break;
            case "car":
                this.car = new String(bytes, start, length);
                break;
            case "model":
                this.model = new String(bytes, start, length);
                break;
            case "insurance":
                this.insurance = new String(bytes, start, length);
            case "contract_date":
                this.contract = new String(bytes, start, length);

        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ( localName.equals("insurance_record") ) {
            double val = 0.0;
            try {
                val = Double.parseDouble(this.insurance);
            } catch (NumberFormatException e) {
                LOG.severe(e.getMessage());
            }

            if ( subtotales.containsKey(this.model) ) {
                double sum = subtotales.get(this.model);
                subtotales.put( this.model, sum + val );
                int a = contadorMap.get(this.model);
                contadorMap.put(this.model, a+1);
            } else {
                subtotales.put(this.model, val );
                contadorMap.put(this.model, 1);
            }

            inSales = false;
        }
    }

    private void printRecord() {
        System.out.printf("%4.4s %-10.10s %-10.10s %9.9s %-10.10s %-15.15s\n",
                id, name, lastName, phone, car, model, insurance, contract);
    }



    public static void main(String args[]) {
        if (args.length == 0) {
            LOG.severe("No file to process. Usage is:" + "\njava DeptSalesReport <keyword>");
            return;
        }
        File xmlFile = new File(args[0] );
        insurancePromedio handler = new insurancePromedio();
        handler.process( xmlFile );
    }
}
