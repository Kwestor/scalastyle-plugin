package org.scalastyle.scalastyleplugin.preferences

import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.preferences.IEclipsePreferences
import org.eclipse.core.runtime.preferences.IPreferencesService
import org.eclipse.core.runtime.preferences.InstanceScope
import org.eclipse.jface.dialogs.IDialogConstants
import org.eclipse.jface.dialogs.MessageDialogWithToggle
import org.eclipse.jface.preference.IPreferenceStore
import org.eclipse.jface.preference.PreferencePage
import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.osgi.util.NLS
import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.layout.FormAttachment
import org.eclipse.swt.layout.FormData
import org.eclipse.swt.layout.FormLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Combo
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Group
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Text
import org.eclipse.ui.IWorkbench
import org.eclipse.ui.IWorkbenchPreferencePage
import org.osgi.service.prefs.BackingStoreException
import org.scalastyle.scalastyleplugin.ScalastylePlugin
import org.scalastyle.scalastyleplugin.config.ProjectConfiguration
import org.scalastyle.scalastyleplugin.config.ProjectConfigurations
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.Shell
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.jface.window.Window;
import org.scalastyle.scalastyleplugin.StringUtils._
import org.scalastyle.scalastyleplugin.SwtUtils._
import org.segl.scalastyle._

case class Configuration(name: String, location: String) extends TableLine

case class Configurations(elements: List[Configuration]) extends Container[Configuration]

class ScalastylePreferencePage extends PreferencePage with IWorkbenchPreferencePage {
  /** text field containing the location. */
  var filenameText: Text = _

  /** browse button. */
  var browseButton: Button = _

  setPreferenceStore(ScalastylePlugin.getDefault().getPreferenceStore())

  val NameSorter = new TableSorter[Configuration, String](_.name, true)
  val LocationSorter = new TableSorter[Configuration, String](_.location, true)
  val classLoader = this.getClass().getClassLoader()
  val messageHelper = new MessageHelper(classLoader)
  val model = new Configurations(List[Configuration](Configuration("name", "foobar")))
  
  private[this] val columns = List(
    DialogColumn[Configuration]("Name", SWT.LEFT, NameSorter, 30, { _.name }),
    DialogColumn[Configuration]("Location", SWT.LEFT, LocationSorter, 70, { _.location })
  )

  def createContents(parent: Composite): Control = {
    noDefaultAndApplyButton()
    
    // there is a bug in SWT which means you can't mix & match FormLayout
    // and GridLayout, you get java.lang.ClassCastException: org.eclipse.swt.layout.GridData cannot be cast to org.eclipse.swt.layout.FormData
    // so we set the parent layout here
    
    parent.setLayout(gridLayout())
    parent.setLayoutData(gridData())
    val parentComposite = composite(parent)

//    val configurationsGroup = group(parentComposite, "Configurations");

//    table(configurationsGroup, model, columns, new ModelContentProvider(model), new PropertiesLabelProvider(columns), setSelection, refresh)

    val generalComposite = createGeneralContents(parentComposite)

    parentComposite
  }
  
  def setSelection(selection: Configuration): Unit = {
    println("setSelection")
  }
  
  def refresh(): Unit = {
    println("refresh")
  }
  
  def createGeneralContents(parent: Composite): Control = {
    val generalComposite = group(parent, "General", layout = gridLayout(1, 10))

    val configurationComposite = composite(generalComposite, layout = gridLayout(4, 10), layoutData=Some(gridData(GridData.FILL_HORIZONTAL)))

    val configurationLabel = label(configurationComposite, "Configuration (relative to project)")

    val configuration = ProjectConfigurations.get()

    filenameText = text(configurationComposite, configuration.file.getOrElse(""), true, false)

    val browseButton = button(configurationComposite, "Browse", true, {
      browseForFile(this.getShell(), "Select a scalastyle configuration file") match {
        case Some(file) => filenameText.setText(file.getFullPath().toString())
        case None => 
      }
    })

    val editButton = button(configurationComposite, "Edit", true, {
      if (!isEmpty(filenameText.getText())) {
        editConfiguration(filenameText.getText());
      } else {
        println("message saying must specify a file name")
      }
    })

    generalComposite
  }

  private[this] def browseForFile(shell: Shell, title: String): Option[IFile] = {
    val dialog = new ElementTreeSelectionDialog(shell, new WorkbenchLabelProvider(), new WorkbenchContentProvider());
    dialog.setTitle(title)
    dialog.setMessage(title)
    dialog.setBlockOnOpen(true)
    dialog.setAllowMultiple(false)
    // TODO add initial selection
//    val initial = file match {
//      case Some(name) => name
//      case None => null
//    }
//    dialog.setInitialSelection(initial)
    dialog.setInput(ScalastylePlugin.getWorkspace().getRoot())
    
    dialog.setValidator(new ISelectionStatusValidator() {
      def validate(selection: Array[Object]): IStatus = {
        val valid = selection.length == 1 && selection(0).isInstanceOf[IFile]
        new Status(if (valid) IStatus.OK else IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR, "", null)
      }
    });
    
    if (Window.OK == dialog.open()) {
      val result = dialog.getResult();
      val checkFile = result(0).asInstanceOf[IFile];
      Some(checkFile)
    } else {
      None
    }
  }

  private[this] def editConfiguration(file: String) = {
    val dialog = new ScalastyleConfigurationDialog(getShell(), file);
    dialog.setBlockOnOpen(true);
    dialog.open();
  }

  def init(workbench: IWorkbench): Unit = {}

  override def performOk(): Boolean = {
    try {
      val configurationFile = filenameText.getText();
      ProjectConfigurations.save(ProjectConfiguration(toOption(configurationFile)))

      true
    } catch {
      case e: Exception =>
        {
          // TODO log something here
          println("caught exception")
          e.printStackTrace(System.out)
        }
        false
    }
  }
}
