import com.apollographql.apollo3.ast.GQLDefinition
import com.apollographql.apollo3.ast.GQLDocument
import com.apollographql.apollo3.ast.GQLField
import com.apollographql.apollo3.ast.GQLFragmentDefinition
import com.apollographql.apollo3.ast.GQLFragmentSpread
import com.apollographql.apollo3.ast.GQLInlineFragment
import com.apollographql.apollo3.ast.GQLSelection
import com.apollographql.apollo3.ast.parseAsGQLDocument
import okio.BufferedSource
import okio.buffer
import okio.source
import java.io.File

fun main() {
    val bufferedSource: BufferedSource = File("document.graphql").source().buffer()
    val gqlDocument: GQLDocument = bufferedSource.parseAsGQLDocument().valueAssertNoErrors()
    for (gqlDefinition in gqlDocument.definitions) {
        if (gqlDefinition is GQLFragmentDefinition) {
            forEachFieldRecursively(gqlDocument.definitions, gqlDefinition.selectionSet.selections) { println(it.name) }
        }
    }

}

private fun forEachFieldRecursively(allDefinitions: List<GQLDefinition>, gqlSelectionList: List<GQLSelection>, block: (GQLField) -> Unit) {
    for (gqlSelection in gqlSelectionList) {
        when (gqlSelection) {
            is GQLField -> block(gqlSelection)
            is GQLInlineFragment -> forEachFieldRecursively(allDefinitions, gqlSelection.selectionSet.selections, block)
            is GQLFragmentSpread -> {
                // Find the referenced fragment in the document
                val referencedFragmentDefinition = allDefinitions.find { it is GQLFragmentDefinition && it.name == gqlSelection.name } as GQLFragmentDefinition
                forEachFieldRecursively(allDefinitions, referencedFragmentDefinition.selectionSet.selections, block)
            }
        }
    }
}
