// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.scalastyle.scalastyleplugin

import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.BundleContext
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IWorkspace;

object ScalastylePlugin {
  val PLUGIN_ID = "scalastyle-plugin" //$NON-NLS-1$
  val PreferenceConfigurationFile = "scalastyle.preferenceConfigurationFile"

  private var plugin: ScalastylePlugin = _

  def getDefault(): ScalastylePlugin = plugin

  def getImageDescriptor(path: String): ImageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path)

  def getWorkspace(): IWorkspace = ResourcesPlugin.getWorkspace();
}

/**
 * The activator class controls the plug-in life cycle
 */
class ScalastylePlugin extends AbstractUIPlugin {
  override def start(context: BundleContext): Unit = {
    super.start(context)
    ScalastylePlugin.plugin = this
  }

  override def stop(context: BundleContext): Unit = {
    ScalastylePlugin.plugin = null
    super.stop(context)
  }
}
