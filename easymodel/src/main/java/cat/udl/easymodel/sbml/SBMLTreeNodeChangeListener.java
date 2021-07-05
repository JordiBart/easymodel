package cat.udl.easymodel.sbml;

import java.beans.PropertyChangeEvent;

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.util.TreeNodeChangeListener;
import org.sbml.jsbml.util.TreeNodeRemovedEvent;

public class SBMLTreeNodeChangeListener implements TreeNodeChangeListener {
	public SBMLTreeNodeChangeListener() {
		super();
	}

	/*
	 * Methods for TreeNodeChangeListener, to respond to events from
	 * SBaseChangedListener.
	 */
	@Override
	public void nodeAdded(TreeNode sb) {
		System.out.println("[ADD] " + sb);
	}

	@Override
	public void nodeRemoved(TreeNodeRemovedEvent evt) {
		System.out.println("[RMV] " + evt.getSource());
	}

	@Override
	public void propertyChange(PropertyChangeEvent ev) {
		System.out.println("[CHG] " + ev);
	}
}
