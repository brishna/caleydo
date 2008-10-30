package org.caleydo.core.view.opengl.canvas.remote.bucket;

import gleem.linalg.Mat4f;
import gleem.linalg.Rotf;
import gleem.linalg.Vec3f;
import java.util.ArrayList;
import java.util.Iterator;
import javax.media.opengl.GL;
import org.caleydo.core.data.mapping.EIDType;
import org.caleydo.core.data.selection.SelectedElementRep;
import org.caleydo.core.view.opengl.canvas.remote.AGLConnectionLineRenderer;
import org.caleydo.core.view.opengl.renderstyle.ConnectionLineRenderStyle;
import org.caleydo.core.view.opengl.util.hierarchy.RemoteHierarchyLevel;

/**
 * Specialized connection line renderer for bucket view.
 * 
 * @author Marc Streit
 */
public class GLConnectionLineRendererBucket
	extends AGLConnectionLineRenderer
{

	/**
	 * Constructor.
	 * 
	 * @param underInteractionLayer
	 * @param stackLayer
	 * @param poolLayer
	 */
	public GLConnectionLineRendererBucket(final RemoteHierarchyLevel underInteractionLayer,
			final RemoteHierarchyLevel stackLayer, final RemoteHierarchyLevel poolLayer)
	{
		super(underInteractionLayer, stackLayer, poolLayer);
	}

	@Override
	protected void renderConnectionLines(final GL gl)
	{

		Vec3f vecTranslation;
		Vec3f vecScale;

		Rotf rotation;
		Mat4f matSrc = new Mat4f();
		Mat4f matDest = new Mat4f();
		matSrc.makeIdent();
		matDest.makeIdent();

		for (EIDType idType : connectedElementRepManager.getOccuringIDTypes())
		{

			Iterator<Integer> iterSelectedElementID = connectedElementRepManager
					.getIDList(idType).iterator();

			ArrayList<ArrayList<Vec3f>> alPointLists = null;// 

			while (iterSelectedElementID.hasNext())
			{
				int iSelectedElementID = iterSelectedElementID.next();
				Iterator<SelectedElementRep> iterSelectedElementRep = connectedElementRepManager
						.getSelectedElementRepsByElementID(idType, iSelectedElementID).iterator();

				while (iterSelectedElementRep.hasNext())
				{
					SelectedElementRep selectedElementRep = iterSelectedElementRep.next();

					RemoteHierarchyLevel activeLayer = null;
					// Check if element is in under interaction layer
					if (underInteractionLayer.containsElement(selectedElementRep
							.getContainingViewID()))
					{
						activeLayer = underInteractionLayer;
					}
					else if (stackLayer.containsElement(selectedElementRep
							.getContainingViewID()))
					{
						activeLayer = stackLayer;
					}

					if (activeLayer != null)
					{
						vecTranslation = activeLayer.getTransformByElementId(
								selectedElementRep.getContainingViewID()).getTranslation();
						vecScale = activeLayer.getTransformByElementId(
								selectedElementRep.getContainingViewID()).getScale();
						rotation = activeLayer.getTransformByElementId(
								selectedElementRep.getContainingViewID()).getRotation();

						ArrayList<Vec3f> alPoints = selectedElementRep.getPoints();
						ArrayList<Vec3f> alPointsTransformed = new ArrayList<Vec3f>();

						for (Vec3f vecCurrentPoint : alPoints)
						{
							alPointsTransformed.add(transform(vecCurrentPoint, vecTranslation,
									vecScale, rotation));
						}
						int iKey = selectedElementRep.getContainingViewID();

						alPointLists = hashViewToPointLists.get(iKey);
						if (alPointLists == null)
						{
							alPointLists = new ArrayList<ArrayList<Vec3f>>();
							hashViewToPointLists.put(iKey, alPointLists);
						}

						alPointLists.add(alPointsTransformed);
					}
				}

				if (hashViewToPointLists.size() > 1)
				{
					float[] fArColor;
					if (idType == EIDType.EXPRESSION_INDEX)
						fArColor = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR_1;
					else if (idType == EIDType.EXPERIMENT_INDEX)
						fArColor = ConnectionLineRenderStyle.CONNECTION_LINE_COLOR_2;
					else
						throw new IllegalStateException("No color defined for the connection type " +idType);
					
					renderLineBundling(gl, fArColor);
					hashViewToPointLists.clear();
				}
			}
		}
	}
}