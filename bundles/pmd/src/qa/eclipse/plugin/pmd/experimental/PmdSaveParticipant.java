/***************************************************************************
 * Copyright (C) 2019 Christian Wulf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package qa.eclipse.plugin.pmd.experimental;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 *
 * @author Christian Wulf
 *
 */
public class PmdSaveParticipant implements ISaveParticipant {

	@Override
	public void doneSaving(final ISaveContext context) {
		final IPath[] files = context.getFiles();
	}

	@Override
	public void prepareToSave(final ISaveContext context) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void rollback(final ISaveContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saving(final ISaveContext context) throws CoreException {
		// TODO Auto-generated method stub

	}

}
