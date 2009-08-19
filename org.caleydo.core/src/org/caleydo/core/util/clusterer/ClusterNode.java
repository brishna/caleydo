package org.caleydo.core.util.clusterer;

import gleem.linalg.Vec3f;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.caleydo.core.data.selection.ESelectionType;

/**
 * Implementation of a node used in the cluster tree. Cluster node contains information needed in the
 * dendrogram and the radial hierarchy view. Additionally cluster node implements {@link Comparable}.
 * 
 * @author Bernhard Schlegl
 */
@XmlRootElement(name = "node")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClusterNode
	implements Comparable<ClusterNode> {

	@XmlAttribute
	private String sNodeName;
	@XmlElement
	private int iClusterNr;
	@XmlElement
	private float fCoefficient;
	@XmlElement
	private int iHierarchyDepth;
	@XmlElement
	private int iNrElements;
	@XmlElement
	private Vec3f vPos;
	@XmlElement
	private ESelectionType eSelectionType;
	@XmlElement
	private boolean bIsRootNode;
	@XmlElement
	private float fAverageExpressionValue;
	@XmlElement
	private float fStandardDeviation;

	private boolean bIsPartOfSubTree = false;
	private Vec3f vPosSubTree;

	// @XmlElement
	// private float[] fArRepresentativeElement;

	public ClusterNode() {

	}

	public ClusterNode(String sNodeName, int iClusterNr, float fCoefficient, int iDepth, boolean bIsRootNode) {
		this.sNodeName = sNodeName;
		this.iClusterNr = iClusterNr;
		this.fCoefficient = fCoefficient;
		this.iHierarchyDepth = iDepth;
		this.bIsRootNode = bIsRootNode;
		this.eSelectionType = ESelectionType.NORMAL;
		this.fAverageExpressionValue = 0f;
		this.fStandardDeviation = 0f;
	}

	@Override
	public int compareTo(ClusterNode node) {
		return iClusterNr - node.iClusterNr;
	}

	public String getNodeName() {
		return sNodeName;
	}

	public float getCoefficient() {
		return fCoefficient;
	}

	public int getClusterNr() {
		return iClusterNr;
	}

	@Override
	public String toString() {
		return sNodeName;
	}

	public void setDepth(int iDepth) {
		this.iHierarchyDepth = iDepth;
	}

	public int getDepth() {
		return iHierarchyDepth;
	}

	public void setNrElements(int iNrElements) {
		this.iNrElements = iNrElements;
	}

	public int getNrElements() {
		return iNrElements;
	}

	public void setPos(Vec3f vPos) {
		this.vPos = vPos;
	}

	public Vec3f getPos() {
		return vPos;
	}

	public void setSelectionType(ESelectionType eSelectionType) {
		this.eSelectionType = eSelectionType;
	}

	public ESelectionType getSelectionType() {
		return eSelectionType;
	}

	public void toggleSelectionType() {
		this.eSelectionType =
			(eSelectionType == ESelectionType.SELECTION) ? ESelectionType.NORMAL : ESelectionType.SELECTION;
	}

	public boolean isRootNode() {
		return bIsRootNode;
	}

	public void setAverageExpressionValue(float fAverageExpressionValue) {
		this.fAverageExpressionValue = fAverageExpressionValue;
	}

	public float getAverageExpressionValue() {
		return fAverageExpressionValue;
	}

	public void setStandardDeviation(float fStandardDeviation) {
		this.fStandardDeviation = fStandardDeviation;
	}

	public float getStandardDeviation() {
		return fStandardDeviation;
	}

	public void setIsPartOfSubTree(boolean bIsPartOfSubTree) {
		this.bIsPartOfSubTree = bIsPartOfSubTree;
	}

	public boolean isPartOfSubTree() {
		return bIsPartOfSubTree;
	}

	public void setPosSubTree(Vec3f vPosSubTree) {
		this.vPosSubTree = vPosSubTree;
	}

	public Vec3f getPosSubTree() {
		return vPosSubTree;
	}

	// public void setRepresentativeElement(float[] fArRepresentativeElement) {
	// this.fArRepresentativeElement = fArRepresentativeElement;
	// }
	//
	// public float[] getRepresentativeElement() {
	// return fArRepresentativeElement;
	// }
}
