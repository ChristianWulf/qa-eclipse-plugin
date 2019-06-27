/***************************************************************************
 * Copyright (C) 2019
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
package qa.eclipse.plugin.bundles.common.view;

/**
 * @author Reiner Jung
 *
 */
public enum ESortProperty {
	SORT_PROP_PRIORITY, // sort by error priority
	SORT_PROP_RULE_NAME, // sort by check name
	SORT_PROP_LINENUMBER, //
	SORT_PROP_PROJECTNAME, //
	SORT_PROP_RULESET_NAME, //
	SORT_PROP_MESSAGE, //
	SORT_PROP_PATH, // sort by directory of the files
	SORT_PROP_FILENAME
}
