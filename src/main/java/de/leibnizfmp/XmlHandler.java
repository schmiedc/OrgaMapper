package de.leibnizfmp;

import ij.IJ;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.DoubleUnaryOperator;

/**
 * implements reading, writing of the xml settings file
 */
class XmlHandler {

    // settings for nucleus settings
    float kernelSizeNuc;
    double rollingBallRadiusNuc;
    String thresholdNuc;
    int erosionNuc;
    double minSizeNuc;
    double maxSizeNuc;
    double lowCircNuc;
    double highCircNuc;

    // settings for cell area segmentation
    float kernelSizeCellArea;
    double rollingBallRadiusCellArea;
    int manualThresholdCellArea;

    // settings for cell separator
    double sigmaGaussCellSep;
    double prominenceCellSep;

    // settings for cell filter size
    double minCellSize;
    double maxCellSize;
    double lowCircCellSize;
    double highCircCelLSize;

    // settings for organelle detection
    double sigmaLoGOrga;
    double prominenceOrga;

    // image settings
    boolean calibrationSetting;
    double pxSizeMicron;
    int nucleusChannel;
    int cytoplasmChannel;
    int organelleChannel;
    int measure;

    String fileFormat;

    /**
     * reads the xml settings file
     *
     * @param filePath for the xml settings file
     * @throws ParserConfigurationException if xml cannot be parsed
     * @throws IOException if xml cannot be read
     * @throws SAXException error message
     */
    void xmlReader(String filePath) throws ParserConfigurationException, IOException, SAXException {

        File xmlFile = new File(filePath);

        // build a document object
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);

        // get nodes of tag name
        kernelSizeNuc = Float.parseFloat(doc.getElementsByTagName("kernelSizeNuc").item(0).getTextContent());
        rollingBallRadiusNuc = Double.parseDouble(doc.getElementsByTagName("rollingBallRadiusNuc").item(0).getTextContent());
        thresholdNuc= String.valueOf(Double.parseDouble(doc.getElementsByTagName("thresholdNuc").item(0).getTextContent()));
        erosionNuc = Integer.parseInt(doc.getElementsByTagName("erosionNuc").item(0).getTextContent());
        minSizeNuc = Double.parseDouble(doc.getElementsByTagName("minSizeNuc").item(0).getTextContent());
        maxSizeNuc= Double.parseDouble(doc.getElementsByTagName("maxSizeNuc").item(0).getTextContent());
        lowCircNuc = Double.parseDouble(doc.getElementsByTagName("lowCircNuc").item(0).getTextContent());
        highCircNuc = Integer.parseInt(doc.getElementsByTagName("highCircNuc").item(0).getTextContent());
        kernelSizeCellArea = Float.parseFloat(doc.getElementsByTagName("kernelSizeCellArea").item(0).getTextContent());
        rollingBallRadiusCellArea = Double.parseDouble(doc.getElementsByTagName("rollingBallRadiusCellArea").item(0).getTextContent());
        manualThresholdCellArea = Integer.parseInt(doc.getElementsByTagName("manualThresholdCellArea").item(0).getTextContent());
        sigmaGaussCellSep = Double.parseDouble(doc.getElementsByTagName("sigmaGaussCellSep").item(0).getTextContent());
        prominenceCellSep = Double.parseDouble(doc.getElementsByTagName("sigmaBackground").item(0).getTextContent());
        minCellSize = Double.parseDouble(doc.getElementsByTagName("minCellSize").item(0).getTextContent());
        maxCellSize = Double.parseDouble(doc.getElementsByTagName("maxCellSize").item(0).getTextContent());
        lowCircCellSize = Double.parseDouble(doc.getElementsByTagName("lowCircCellSize").item(0).getTextContent());
        highCircCelLSize = Double.parseDouble(doc.getElementsByTagName("highCircCelLSize").item(0).getTextContent());
        sigmaLoGOrga = Double.parseDouble(doc.getElementsByTagName("sigmaLoGOrga").item(0).getTextContent());
        prominenceOrga = Double.parseDouble(doc.getElementsByTagName("prominenceOrga").item(0).getTextContent());
        calibrationSetting = Boolean.parseBoolean(doc.getElementsByTagName("calibrationSetting").item(0).getTextContent());
        pxSizeMicron = Double.parseDouble(doc.getElementsByTagName("pxSizeMicron").item(0).getTextContent());
        nucleusChannel = Integer.parseInt(doc.getElementsByTagName("nucleusChannel").item(0).getTextContent());
        cytoplasmChannel = Integer.parseInt(doc.getElementsByTagName("cytoplasmChannel").item(0).getTextContent());
        organelleChannel= Integer.parseInt(doc.getElementsByTagName("organelleChannel").item(0).getTextContent());
        measure = Integer.parseInt(doc.getElementsByTagName("measure").item(0).getTextContent());
        fileFormat = doc.getElementsByTagName("fileFormat").item(0).getTextContent();

        IJ.log("Loaded settings file from: " + filePath);

    }

    /**
     * writes xml settings file
     *
     * @param outputPath directory for saving results
     * @param getKernelSizeNuc filter size for filtering nuclei
     * @param getRollingBallRadiusNuc rolling ball background radius for neuclei
     * @param getThresholdNuc threshold nucleus segmentation
     * @param getErosionNuc erosion for nucleus segmentation
     * @param getMinSizeNuc minimum size of nuclei
     * @param getMaxSizeNuc maximum size of nuclei
     * @param getLowCircNuc minimum circularity of nuclei
     * @param getHighCircNuc maximum circularity of nuclei
     * @param getKernelSizeCellArea filter size for nuclei segmentation
     * @param getRollingBallRadiusCellArea rolling ball radius for cell segmentation
     * @param getManualThresholdCellArea manual threshold for cell area
     * @param getSigmaGaussCellSep sigma gaussian blur
     * @param getProminenceCellSep prominence for watershed
     * @param getMinCellSize minimum cell size
     * @param getMaxCellSize maximum cell size
     * @param getLowCircCellSize minimum circularity of cells
     * @param getHighCircCelLSize maximum circularity of cells
     * @param getSigmaLoGOrga sigma for LoG3D for organelle detection
     * @param getProminenceOrga prominence for organelle detection
     * @param getCalibrationSetting global intensity threshold for background segmentation
     * @param getPxSizeMicron pixel size
     * @param getNucleusChannel number for nucleus channel
     * @param getCytoplasmChannel number for cytoplams channel
     * @param getOrganelleChannel number for organelle channel
     * @param getMeasure number for measurement channel
     * @param getFileFormat string for file format ending
     */
    void xmlWriter(String outputPath,
                   String fileName,
                   float getKernelSizeNuc,
                   double getRollingBallRadiusNuc,
                   String getThresholdNuc,
                   int getErosionNuc,
                   double getMinSizeNuc,
                   double getMaxSizeNuc,
                   double getLowCircNuc,
                   double getHighCircNuc,
                   float getKernelSizeCellArea,
                   double getRollingBallRadiusCellArea,
                   int getManualThresholdCellArea,
                   double getSigmaGaussCellSep,
                   double getProminenceCellSep,
                   double getMinCellSize,
                   double getMaxCellSize,
                   double getLowCircCellSize,
                   double getHighCircCelLSize,
                   double getSigmaLoGOrga,
                   double getProminenceOrga,
                   boolean getCalibrationSetting,
                   double getPxSizeMicron,
                   int getNucleusChannel,
                   int getCytoplasmChannel,
                   int getOrganelleChannel,
                   int getMeasure,
                   String getFileFormat){

        // Write the content into XML file
        String filePath = outputPath + fileName;

        try
        {

            String kernelSizeNuc = Float.toString(getKernelSizeNuc);
            String rollingBallRadiusNuc = Double.toString(getRollingBallRadiusNuc);
            String erosionNuc = Integer.toString(getErosionNuc);
            String minSizeNuc = Double.toString(getMinSizeNuc);
            String maxSizeNuc = Double.toString(getMaxSizeNuc);
            String lowCircNuc = Double.toString(getLowCircNuc);
            String highCircNuc = Double.toString(getHighCircNuc);
            String kernelSizeCellArea = Float.toString(getKernelSizeCellArea);
            String rollingBallRadiusCellArea = Double.toString(getRollingBallRadiusCellArea);
            String manualThresholdCellArea = Integer.toString(getManualThresholdCellArea);
            String sigmaGaussCellSep = Double.toString(getSigmaGaussCellSep);
            String prominenceCellSep = Double.toString(getProminenceCellSep);
            String minCellSize = Double.toString(getMinCellSize);
            String maxCellSize = Double.toString(getMaxCellSize);
            String lowCircCellSize = Double.toString(getLowCircCellSize);
            String highCircCellSize = Double.toString(getHighCircCelLSize);
            String sigmaLoGOrga = Double.toString(getSigmaLoGOrga);
            String prominenceOrga = Double.toString(getProminenceOrga);
            String calibrationSettings = Boolean.toString(getCalibrationSetting);
            String pxSizeMicron = Double.toString(getPxSizeMicron);
            String nucleusChannel = Integer.toString(getNucleusChannel);
            String cytoplasmChannel = Integer.toString(getCytoplasmChannel);
            String organelleChannel = Integer.toString(getOrganelleChannel);
            String measureChannel = Integer.toString(getMeasure);

            // create document builder
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            // pHluorinSettings as root element
            Element rootElement = doc.createElement("pHluorinSettings");
            doc.appendChild(rootElement);

            Element settingsKernelSizeNuc = doc.createElement("kernelSizeNuc");
            settingsKernelSizeNuc.setTextContent(kernelSizeNuc);
            rootElement.appendChild(settingsKernelSizeNuc);

            Element settingsRollingBallRadiusNuc = doc.createElement("rollingBallRadiusNuc");
            settingsRollingBallRadiusNuc.setTextContent(rollingBallRadiusNuc);
            rootElement.appendChild(settingsRollingBallRadiusNuc);

            Element settingsThresholdNuc = doc.createElement("thresholdNuc");
            settingsThresholdNuc.setTextContent(getThresholdNuc);
            rootElement.appendChild(settingsThresholdNuc);

            Element settingsErosionNuc = doc.createElement("erosionNuc");
            settingsErosionNuc.setTextContent(erosionNuc);
            rootElement.appendChild(settingsErosionNuc);

            Element setMinSizeNuc = doc.createElement("minSizeNuc");
            setMinSizeNuc.setTextContent(minSizeNuc);
            rootElement.appendChild(setMinSizeNuc);

            Element setMaxSizeNuc = doc.createElement("maxSizeNuc");
            setMaxSizeNuc.setTextContent(maxSizeNuc);
            rootElement.appendChild(setMaxSizeNuc);

            Element setlowCircNuc = doc.createElement("lowCircNuc");
            setlowCircNuc.setTextContent(lowCircNuc);
            rootElement.appendChild(setlowCircNuc);

            Element sethighCircNuc = doc.createElement("highCircNuc");
            sethighCircNuc.setTextContent(highCircNuc);
            rootElement.appendChild(sethighCircNuc);

            Element setkernelSizeCellArea = doc.createElement("kernelSizeCellArea");
            setkernelSizeCellArea.setTextContent(kernelSizeCellArea);
            rootElement.appendChild(setkernelSizeCellArea);

            Element setrollingBallRadiusCellArea = doc.createElement("rollingBallRadiusCellArea");
            setrollingBallRadiusCellArea.setTextContent(rollingBallRadiusCellArea);
            rootElement.appendChild(setrollingBallRadiusCellArea);

            Element setmanualThresholdCellArea = doc.createElement("manualThresholdCellArea");
            setmanualThresholdCellArea.setTextContent(manualThresholdCellArea);
            rootElement.appendChild(setmanualThresholdCellArea);

            Element setsigmaGaussCellSep = doc.createElement("sigmaGaussCellSep");
            setsigmaGaussCellSep.setTextContent(sigmaGaussCellSep);
            rootElement.appendChild(setsigmaGaussCellSep);

            Element setprominenceCellSep = doc.createElement("prominenceCellSep");
            setprominenceCellSep.setTextContent(prominenceCellSep);
            rootElement.appendChild(setprominenceCellSep);

            Element setminCellSize = doc.createElement("minCellSize");
            setminCellSize.setTextContent(minCellSize);
            rootElement.appendChild(setminCellSize);

            Element setmaxCellSize = doc.createElement("maxCellSize");
            setmaxCellSize.setTextContent(maxCellSize);
            rootElement.appendChild(setmaxCellSize);

            Element setlowCircCellSize = doc.createElement("lowCircCellSize");
            setlowCircCellSize.setTextContent(lowCircCellSize);
            rootElement.appendChild(setlowCircCellSize);

            Element sethighCircCellSize = doc.createElement("highCircCellSize");
            sethighCircCellSize.setTextContent(highCircCellSize);
            rootElement.appendChild(sethighCircCellSize);

            Element setsigmaLoGOrga = doc.createElement("sigmaLoGOrga");
            setsigmaLoGOrga.setTextContent(sigmaLoGOrga);
            rootElement.appendChild(setsigmaLoGOrga);

            Element setprominenceOrga = doc.createElement("prominenceOrga");
            setprominenceOrga.setTextContent(prominenceOrga);
            rootElement.appendChild(setprominenceOrga);

            Element setcalibrationSettings = doc.createElement("calibrationSettings");
            setcalibrationSettings.setTextContent(calibrationSettings);
            rootElement.appendChild(setcalibrationSettings);

            Element setpxSizeMicron = doc.createElement("pxSizeMicron");
            setpxSizeMicron.setTextContent(pxSizeMicron);
            rootElement.appendChild(setpxSizeMicron);

            Element setnucleusChannel = doc.createElement("nucleusChannel");
            setnucleusChannel.setTextContent(nucleusChannel);
            rootElement.appendChild(setnucleusChannel);

            Element setcytoplasmChannel = doc.createElement("cytoplasmChannel");
            setcytoplasmChannel.setTextContent(cytoplasmChannel);
            rootElement.appendChild(setcytoplasmChannel);

            Element setorganelleChannel = doc.createElement("organelleChannel");
            setorganelleChannel.setTextContent(organelleChannel);
            rootElement.appendChild(setorganelleChannel);

            Element setmeasureChannel = doc.createElement("measureChannel");
            setmeasureChannel.setTextContent(measureChannel);
            rootElement.appendChild(setmeasureChannel);

            Element setFileFormat = doc.createElement("FileFormat");
            setFileFormat.setTextContent(getFileFormat);
            rootElement.appendChild(setFileFormat);

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new FileOutputStream(filePath));

            // create transformer factory
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // Beautify the format of the resulted XML using an ident with 4 spaces
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(source, result);

            IJ.log("Saved settings file: " + filePath);

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            IJ.log("WARNING: was not able to save settings file to: " + filePath);
        }

    }

}
