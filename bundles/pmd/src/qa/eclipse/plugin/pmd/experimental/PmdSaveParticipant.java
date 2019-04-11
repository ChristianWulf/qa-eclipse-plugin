package qa.eclipse.plugin.pmd.experimental;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class PmdSaveParticipant implements ISaveParticipant {

	@Override
	public void doneSaving(ISaveContext context) {
		IPath[] files = context.getFiles();
		System.out.println("files: " + files);
	}

	@Override
	public void prepareToSave(ISaveContext context) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void rollback(ISaveContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saving(ISaveContext context) throws CoreException {
		// TODO Auto-generated method stub

	}

}
