package org.caleydo.datadomain.pathway.manager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.caleydo.core.manager.AManager;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.specialized.Organism;
import org.caleydo.core.util.logging.Logger;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.parser.BioCartaPathwayImageMapSaxHandler;
import org.caleydo.datadomain.pathway.parser.KgmlSaxHandler;
import org.caleydo.datadomain.pathway.parser.PathwayImageMap;
import org.caleydo.util.graph.EGraphItemHierarchy;
import org.caleydo.util.graph.core.Graph;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * The pathway manager is in charge of creating and handling the pathways. The
 * class is implemented as a singleton.
 * 
 * @author Marc Streit
 */
public class PathwayManager extends AManager<PathwayGraph> {

	private static PathwayManager pathwayManager;

	private PathwayParserManager xmlParserManager;

	public IPathwayResourceLoader keggPathwayResourceLoader;
	public IPathwayResourceLoader biocartaPathwayResourceLoader;

	private HashMap<PathwayGraph, Boolean> hashPathwayToVisibilityState;

	private HashMap<String, PathwayGraph> hashPathwayTitleToPathway;

	private HashMap<PathwayDatabaseType, PathwayDatabase> hashPathwayDatabase;

	/**
	 * Root pathway contains all nodes that are loaded into the system.
	 * Therefore it represents the overall topological network. (The root
	 * pathway is independent from the representation of the nodes.)
	 */
	private Graph rootPathwayGraph;

	/**
	 * Used for pathways where only images can be loaded. The image map defines
	 * the clickable regions on that pathway image.
	 */
	private PathwayImageMap currentPathwayImageMap;

	private PathwayGraph currentPathwayGraph;

	private boolean pathwayLoadingFinished;

	private PathwayManager() {

	}

	/**
	 * Returns the pathway manager as a singleton object. When first called the
	 * manager is created (lazy).
	 * 
	 * @return singleton PathwayManager instance
	 */
	public static PathwayManager get() {
		if (pathwayManager == null) {
			pathwayManager = new PathwayManager();
			pathwayManager.init();
		}
		return pathwayManager;
	}

	private void init() {
		hashPathwayTitleToPathway = new HashMap<String, PathwayGraph>();
		hashPathwayDatabase = new HashMap<PathwayDatabaseType, PathwayDatabase>();
		hashPathwayToVisibilityState = new HashMap<PathwayGraph, Boolean>();

		rootPathwayGraph = new Graph(0);

		xmlParserManager = new PathwayParserManager();

		KgmlSaxHandler kgmlParser = new KgmlSaxHandler();
		xmlParserManager.registerAndInitSaxHandler(kgmlParser);
		BioCartaPathwayImageMapSaxHandler biocartaPathwayParser = new BioCartaPathwayImageMapSaxHandler();
		xmlParserManager.registerAndInitSaxHandler(biocartaPathwayParser);
	}

	public PathwayDatabase createPathwayDatabase(final PathwayDatabaseType type,
			final String XMLPath, final String imagePath, final String imageMapPath) {

		// Check if requested pathway database is already loaded (e.g. using
		// caching)
		if (hashPathwayDatabase.containsKey(type))
			return hashPathwayDatabase.get(type);

		PathwayDatabase pathwayDatabase = new PathwayDatabase(type, XMLPath, imagePath,
				imagePath);

		hashPathwayDatabase.put(type, pathwayDatabase);

		Logger.log(new Status(IStatus.INFO, this.toString(),
				"Setting pathway loading path: database-type:[" + type + "] "
						+ "xml-path:[" + pathwayDatabase.getXMLPath() + "] image-path:["
						+ pathwayDatabase.getImagePath() + "] image-map-path:["
						+ pathwayDatabase.getImageMapPath() + "]"));

		return pathwayDatabase;
	}

	public PathwayGraph createPathway(final PathwayDatabaseType type, final String sName,
			final String sTitle, final String sImageLink, final String sExternalLink) {
		PathwayGraph pathway = new PathwayGraph(type, sName, sTitle, sImageLink,
				sExternalLink);

		registerItem(pathway);
		hashPathwayTitleToPathway.put(sTitle, pathway);
		hashPathwayToVisibilityState.put(pathway, false);

		rootPathwayGraph.addGraph(pathway, EGraphItemHierarchy.GRAPH_CHILDREN);

		currentPathwayGraph = pathway;

		return pathway;
	}

	public PathwayGraph searchPathwayByName(final String sPathwayName,
			PathwayDatabaseType ePathwayDatabaseType) {
		waitUntilPathwayLoadingIsFinished();

		Iterator<String> iterPathwayName = hashPathwayTitleToPathway.keySet().iterator();
		Pattern pattern = Pattern.compile(sPathwayName, Pattern.CASE_INSENSITIVE);
		Matcher regexMatcher;
		String sTmpPathwayName;

		while (iterPathwayName.hasNext()) {
			sTmpPathwayName = iterPathwayName.next();
			regexMatcher = pattern.matcher(sTmpPathwayName);

			if (regexMatcher.find()) {
				PathwayGraph pathway = hashPathwayTitleToPathway.get(sTmpPathwayName);

				// Ignore the found pathway if it has the same name but is
				// contained
				// in a different database
				if (getItem(pathway.getID()).getType() != ePathwayDatabaseType) {
					continue;
				}

				return pathway;
			}
		}

		return null;
	}

	public Graph getRootPathway() {
		return rootPathwayGraph;
	}

	@Override
	public Collection<PathwayGraph> getAllItems() {
		waitUntilPathwayLoadingIsFinished();

		return super.getAllItems();
	}

	public void setPathwayVisibilityState(final PathwayGraph pathway,
			final boolean bVisibilityState) {
		waitUntilPathwayLoadingIsFinished();

		hashPathwayToVisibilityState.put(pathway, bVisibilityState);
	}

	public void resetPathwayVisiblityState() {
		waitUntilPathwayLoadingIsFinished();

		for (PathwayGraph pathway : hashPathwayToVisibilityState.keySet()) {
			hashPathwayToVisibilityState.put(pathway, false);
		}
	}

	public boolean isPathwayVisible(final PathwayGraph pathway) {
		waitUntilPathwayLoadingIsFinished();

		return hashPathwayToVisibilityState.get(pathway);
	}

	public void createPathwayImageMap(final String sImageLink) {
		currentPathwayImageMap = new PathwayImageMap(sImageLink);
	}

	public PathwayImageMap getCurrentPathwayImageMap() {
		return currentPathwayImageMap;
	}

	public PathwayDatabase getPathwayDatabaseByType(PathwayDatabaseType type) {
		return hashPathwayDatabase.get(type);
	}

	public void notifyPathwayLoadingFinished(boolean pathwayLoadingFinished) {
		this.pathwayLoadingFinished = pathwayLoadingFinished;
	}

	public void waitUntilPathwayLoadingIsFinished() {
		while (!pathwayLoadingFinished) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new IllegalThreadStateException(
						"Pathway loader thread has been interrupted!");
			}
		}
	}

	public boolean isPathwayLoadingFinished() {
		return pathwayLoadingFinished;
	}

	public void createPathwayResourceLoader(PathwayDatabaseType type) {

		IExtensionRegistry reg = Platform.getExtensionRegistry();

		if (type == PathwayDatabaseType.KEGG) {
			IExtensionPoint ep = reg
					.getExtensionPoint("org.caleydo.data.pathway.PathwayResourceLoader");
			IExtension ext = ep
					.getExtension("org.caleydo.data.pathway.kegg.KEGGPathwayResourceLoader");
			IConfigurationElement[] ce = ext.getConfigurationElements();

			try {
				keggPathwayResourceLoader = (IPathwayResourceLoader) ce[0]
						.createExecutableExtension("class");
			} catch (Exception ex) {
				throw new RuntimeException(
						"Could not instantiate KEGG Pathway Resource Loader", ex);
			}
		} else if (type == PathwayDatabaseType.BIOCARTA) {
			IExtensionPoint ep = reg
					.getExtensionPoint("org.caleydo.data.pathway.PathwayResourceLoader");
			IExtension ext = ep
					.getExtension("org.caleydo.data.pathway.biocarta.BioCartaPathwayResourceLoader");
			IConfigurationElement[] ce = ext.getConfigurationElements();

			try {
				biocartaPathwayResourceLoader = (IPathwayResourceLoader) ce[0]
						.createExecutableExtension("class");
			} catch (Exception ex) {
				throw new RuntimeException(
						"Could not instantiate BioCarta Pathway Resource Loader", ex);
			}
		} else {
			throw new IllegalStateException("Unknown pathway database " + type);
		}
	}

	public IPathwayResourceLoader getPathwayResourceLoader(PathwayDatabaseType type) {

		if (type == PathwayDatabaseType.KEGG) {
			return keggPathwayResourceLoader;
		} else if (type == PathwayDatabaseType.BIOCARTA) {
			return biocartaPathwayResourceLoader;
		}

		throw new IllegalStateException("Unknown pathway database " + type);
	}

	public PathwayParserManager getXmlParserManager() {
		return xmlParserManager;
	}

	public void loadPathwaysByType(PathwayDatabase pathwayDatabase) {

		// // Try reading list of files directly from local hard dist
		// File folder = new File(sXMLPath);
		// File[] arFiles = folder.listFiles();

		GeneralManager generalManager = GeneralManager.get();

		Logger.log(new Status(IStatus.INFO, "PathwayLoaderThread", "Start parsing "
				+ pathwayDatabase.getName() + " pathways."));

		BufferedReader file = null;
		String line = null;
		String fileName = "";
		String pathwayPath = pathwayDatabase.getXMLPath();
		IPathwayResourceLoader pathwayResourceLoader = null;
		Organism organism = GeneralManager.get().getBasicInfo().getOrganism();

		if (pathwayDatabase.getType() == PathwayDatabaseType.KEGG) {

			if (organism == Organism.HOMO_SAPIENS) {
				fileName = "data/pathway_list_KEGG_homo_sapiens.txt";
			} else if (organism == Organism.MUS_MUSCULUS) {
				fileName = "data/pathway_list_KEGG_mus_musculus.txt";
			} else {
				throw new IllegalStateException("Cannot load pathways from organism "
						+ organism);
			}

			generalManager.getSWTGUIManager().setProgressBarTextFromExternalThread(
					"Loading KEGG Pathways...");
		} else if (pathwayDatabase.getType() == PathwayDatabaseType.BIOCARTA) {

			if (organism == Organism.HOMO_SAPIENS) {
				fileName = "data/pathway_list_BIOCARTA_homo_sapiens.txt";
			} else if (organism == Organism.MUS_MUSCULUS) {
				fileName = "data/pathway_list_BIOCARTA_mus_musculus.txt";
			} else {
				throw new IllegalStateException("Cannot load pathways from organism "
						+ organism);
			}

			generalManager.getSWTGUIManager().setProgressBarTextFromExternalThread(
					"Loading BioCarta Pathways...");
		}

		PathwayManager.get().createPathwayResourceLoader(pathwayDatabase.getType());
		pathwayResourceLoader = PathwayManager.get().getPathwayResourceLoader(
				pathwayDatabase.getType());

		try {

			if (pathwayDatabase.getType() == PathwayDatabaseType.KEGG
					|| pathwayDatabase.getType() == PathwayDatabaseType.BIOCARTA)
				file = pathwayResourceLoader.getResource(fileName);
			else
				file = GeneralManager.get().getResourceLoader().getResource(fileName);

			StringTokenizer tokenizer;
			String pathwayName;
			PathwayGraph tmpPathwayGraph;
			while ((line = file.readLine()) != null) {
				tokenizer = new StringTokenizer(line, " ");

				pathwayName = tokenizer.nextToken();

				// Skip non pathway files
				if (!pathwayName.endsWith(".xml") && !line.contains("h_")
						&& !line.contains("m_")) {
					continue;
				}

				PathwayManager.get().getXmlParserManager()
						.parseXmlFileByName(pathwayPath + pathwayName);

				currentPathwayGraph.setWidth(Integer.valueOf(tokenizer.nextToken())
						.intValue());
				currentPathwayGraph.setHeight(Integer.valueOf(tokenizer.nextToken())
						.intValue());

				int iImageWidth = currentPathwayGraph.getWidth();
				int iImageHeight = currentPathwayGraph.getHeight();

				if (iImageWidth == -1 || iImageHeight == -1) {
					Logger.log(new Status(IStatus.INFO, "PathwayLoaderThread",
							"Pathway texture width=" + iImageWidth + " / height="
									+ iImageHeight));
				}
			}

		} catch (FileNotFoundException e) {
			throw new IllegalStateException("Pathway list file " + fileName
					+ " not found.");
		} catch (IOException e) {
			throw new IllegalStateException("Error reading data from pathway list file: "
					+ fileName);
		}

		Logger.log(new Status(IStatus.INFO, "PathwayLoaderThread", "Finished parsing "
				+ pathwayDatabase.getName() + " pathways."));
	}
}
