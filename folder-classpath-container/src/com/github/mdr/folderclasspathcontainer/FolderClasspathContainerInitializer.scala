package com.github.mdr.folderclasspathcontainer

import org.eclipse.core.runtime.IPath
import org.eclipse.jdt.core.ClasspathContainerInitializer
import org.eclipse.jdt.core.IJavaProject
import java.util.logging.Logger
import org.eclipse.jdt.core.JavaCore

class FolderClasspathContainerInitializer extends ClasspathContainerInitializer {

  def initialize(containerPath: IPath, project: IJavaProject) {
    val container = new FolderClasspathContainer(containerPath, project)
    //    if (container.isValid) {
    JavaCore.setClasspathContainer(containerPath, Array(project), Array(container), null)
    //    } else
    //      Logger.log(Logger.WARNING, Messages.InvalidContainer + containerPath)
  }

}