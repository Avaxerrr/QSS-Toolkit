package io.github.avaxerrr.qsstoolkit.structure

import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import io.github.avaxerrr.qsstoolkit.psi.QssFile
import io.github.avaxerrr.qsstoolkit.psi.QssRule

class QssStructureViewModel(qssFile: QssFile) :
    StructureViewModelBase(qssFile, QssStructureViewElement(qssFile)),
    StructureViewModel.ElementInfoProvider {

    override fun getSorters(): Array<Sorter> = arrayOf(Sorter.ALPHA_SORTER)

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean {
        return element.value is QssFile
    }

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        return element.value !is QssFile && element.value !is QssRule
    }
}
