package app.shell;

import app.model.Product;
import java.util.Map;
import java.util.HashMap;
import java.io.FileReader;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.EndElement;
import com.google.gson.Gson;

public class XmlReader {

    private String item = "item";

    private Map<String, String> map = new HashMap<String, String>();

    private Product product;

    private Gson gson = new Gson();

    public void process(String file, String[] customMap) {

        for (String line : customMap) {

            if (!line.contains("=>")) {
                continue;
            }

            String[] parts = line.split("=>");

            String from = parts[0].trim();
            String to = parts[1].trim();

            if (to.equals("item")) {
                item = to;
                continue;
            }

            map.put(from, to);
        }

        XMLInputFactory factory = XMLInputFactory.newInstance();

        try {

            XMLEventReader eventReader = factory.createXMLEventReader(new FileReader(file));

            XMLEvent event = eventReader.nextEvent();

            while (eventReader.hasNext()) {

                if (!isStarItem(event)) {
                    event = eventReader.nextEvent();
                    continue;
                }

                product = new Product();

                while (!isEndItem(event)) {

                    event = eventReader.nextEvent();

                    if (event.getEventType() != XMLStreamConstants.START_ELEMENT) {
                        continue;
                    }

                    StartElement element = event.asStartElement();

                    String name = element.getName().getLocalPart();

                    String field = map.get(name);

                    if (field != null && field.isEmpty() == false) {

                        event = eventReader.nextEvent();

                        assignProduct(field, event.asCharacters().getData());
                    }

                    eventReader.nextEvent();
                }
                
                System.out.println(gson.toJson(product));

                event = eventReader.nextEvent();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isStarItem(XMLEvent event) {

        if (event.getEventType() != XMLStreamConstants.START_ELEMENT) {
            return false;
        }

        StartElement node = event.asStartElement();

        return node.getName().getLocalPart().equals(item);
    }

    private boolean isEndItem(XMLEvent event) {

        if (event.getEventType() != XMLStreamConstants.END_ELEMENT) {
            return false;
        }

        EndElement node = event.asEndElement();

        return node.getName().getLocalPart().equals(item);
    }

    private void assignProduct(String field, String value) {

        switch (field) {
            case "id":
                product.setId(value);
                break;

            case "name":
                product.setName(value);
                break;

            case "link":
                product.setLink(value);
                break;

            case "image":
                product.setImage(value);
                break;

            case "price":

                if (product.getPrice() > 0) {
                    break;
                }

                String price = value.replaceAll("[^0-9\\.]", "");
                product.setPrice(new Float(price));
                break;
        }
    }

    public static void main(String[] args) {

        String customMap = "item => item\n"
                + "id => id\n"
                + "title => name\n"
                + "link => link\n"
                + "image_link => image\n"
                + "price => price";

        XmlReader reader = new XmlReader();

        reader.process("example_feed_xml_rss.xml", customMap.split("\n"));
    }
}
