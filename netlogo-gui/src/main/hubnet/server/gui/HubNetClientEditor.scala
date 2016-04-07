// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server.gui

import org.nlogo.api.ModelType
import org.nlogo.core.I18N
import org.nlogo.fileformat
import org.nlogo.core.model.WidgetReader
import org.nlogo.core.{ I18N, Widget => CoreWidget }
import javax.swing.{JMenuBar, JScrollPane, JFrame, ScrollPaneConstants}
import java.awt.{Dimension, BorderLayout, Component}
import java.io.{IOException, StringReader, BufferedReader}
import org.nlogo.window.{Widget, WidgetInfo, MenuBarFactory, InterfaceFactory, GUIWorkspace, AbstractWidgetPanel}

class HubNetClientEditor(workspace: GUIWorkspace,
                         linkParent: Component,
                         iFactory: InterfaceFactory,
                         menuFactory: MenuBarFactory) extends JFrame
        with org.nlogo.window.Event.LinkChild
        with org.nlogo.window.Events.ZoomedEvent.Handler {
  val interfacePanel: AbstractWidgetPanel = iFactory.widgetPanel(workspace)

  locally {
    setTitle(getTitle(workspace.modelNameForDisplay, workspace.getModelDir, workspace.getModelType))
    getContentPane.setLayout(new BorderLayout())
    getContentPane.add(new JScrollPane(interfacePanel,
      ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER)
    import WidgetInfo._
    val buttons = List(button, slider, switch, chooser, input, monitor, plot, note, view)
    getContentPane.add(iFactory.toolbar(interfacePanel, workspace, buttons, this), BorderLayout.NORTH)
    if (System.getProperty("os.name").startsWith("Mac")) {
      val menus = new JMenuBar() {add(menuFactory.createFileMenu())}
      val edit = menuFactory.createEditMenu()
      edit.setEnabled(false)
      menus.add(edit)
      menus.add(menuFactory.createToolsMenu())
      menus.add(menuFactory.createZoomMenu())
      menuFactory.addHelpMenu(menus)
      setJMenuBar(menus)
    }
    setSize(getPreferredSize)
  }

  override def getPreferredSize = if (interfacePanel.empty) new Dimension(700, 550) else super.getPreferredSize
  def getLinkParent = linkParent
  def close() {interfacePanel.removeAllWidgets()}
  override def requestFocus() {interfacePanel.requestFocus()}
  def getWidgetsForSaving: Seq[CoreWidget] = interfacePanel.getWidgetsForSaving

  def getWidgetsAsStrings: Seq[String] = {
    val widgets = getWidgetsForSaving
    def widgetToStrings(w: CoreWidget): List[String] =
      (WidgetReader.format(w, fileformat.hubNetReaders).lines.toSeq :+ "").toList
    widgets.map(widgetToStrings).flatten
  }

  def interfaceWidgets: Seq[CoreWidget] =
    interfacePanel.getWidgetsForSaving

  def load(lines: Array[String], version:String): Unit = {
    interfacePanel.loadWidgets(lines, version, fileformat.hubNetReaders)
    setSize (getPreferredSize)
  }

  def handle(e: org.nlogo.window.Events.ZoomedEvent) {setSize(getPreferredSize)}
  def setTitle(title: String, directory: String, mt: ModelType) {setTitle(getTitle(title, directory, mt))}

  private def getTitle (title:String, directory:String, mt: ModelType) = {
    // on OS X, use standard window title format. otherwise use Windows convention
    val t = if (!System.getProperty("os.name").startsWith("Mac")) {
      title + " - " + I18N.gui.get("menu.tools.hubNetClientEditor")
    } else {
      // 8212 is the unicode value for an em dash. we use the number since
      // we don't want non-ASCII characters in the source files -- AZS 6/14/2005
      I18N.gui.get("menu.tools.hubNetClientEditor") + " " + 8212.toChar + " " + title
    }
    // OS X UI guidelines prohibit paths in title bars, but oh well...
    if (mt == ModelType.Normal) t + " {" + directory + "}" else t
  }
}
