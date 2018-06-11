/*
 * Copyright 2009-2018 the original author or authors.
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
 */
package org.codehaus.groovy.eclipse.dsl.tests

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy
import static org.junit.Assume.assumeTrue

import org.codehaus.groovy.eclipse.codeassist.tests.CompletionTestSuite
import org.codehaus.groovy.eclipse.dsl.DSLPreferencesInitializer
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator
import org.eclipse.core.resources.IFile
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test

final class DSLContentAssistTests extends CompletionTestSuite {

    @BeforeClass
    static void setUpTests() {
        GroovyDSLCoreActivator.default.preferenceStore.setValue(DSLPreferencesInitializer.AUTO_ADD_DSL_SUPPORT, false)
    }

    @Before
    void setUp() {
        assumeTrue(!GroovyDSLCoreActivator.default.isDSLDDisabled())
        addClasspathContainer(GroovyDSLCoreActivator.CLASSPATH_CONTAINER_ID)
        withProject { project ->
            GroovyDSLCoreActivator.default.contextStoreManager.initialize(project, true)
        }

        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'true')
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'true')
    }

    private void createDsld(CharSequence dsld) {
        IFile file = addPlainText(dsld, "${nextUnitName()}.dsld")
        assert file.exists() : "File $file just created, but doesn't exist"
    }

    //--------------------------------------------------------------------------

    @Test
    void testAssignedVariable1() {
        createDsld '''\
            contribute(bind(exprs: assignedVariable())) {
              property name: 'var_' + exprs[0].leftExpression.name
            }
            '''.stripIndent()

        String contents = '''\
            def foo = v
            '''.stripIndent()
        checkUniqueProposal(contents, 'v', 'var_foo')
    }

    @Test
    void testAssignedVariable2() {
        createDsld '''\
            contribute(bind(exprs: assignedVariable('foo'))) {
              property name: 'var_' + exprs[0].leftExpression.name
            }
            '''.stripIndent()

        String contents = '''\
            def foo = v
            '''.stripIndent()
        checkUniqueProposal(contents, 'v', 'var_foo')
    }

    @Test
    void testAssignedVariable2a() {
        createDsld '''\
            contribute(bind(exprs: assignedVariable('boo'))) {
              property name: 'var_' + exprs[0].leftExpression.name
            }
            '''.stripIndent()

        String contents = '''\
            def foo = v
            '''.stripIndent()
        proposalExists(createProposalsAtOffset(contents, getIndexOf(contents, 'v')), 'var_foo', 0)
    }

    @Test
    void testAssignedVariable3() {
        createDsld '''\
            contribute(bind(exprs: assignedVariable(~/f.*/))) {
              property name: 'var_' + exprs[0].leftExpression.name
            }
            '''.stripIndent()

        String contents = '''\
            def foo = v
            '''.stripIndent()
        checkUniqueProposal(contents, 'v', 'var_foo')
    }

    @Test
    void testAssignedVariable3a() {
        createDsld '''\
            contribute(bind(exprs: assignedVariable(~/b.*/))) {
              property name: 'var_' + exprs[0].leftExpression.name
            }
            '''.stripIndent()

        String contents = '''\
            def foo = v
            '''.stripIndent()
        proposalExists(createProposalsAtOffset(contents, getIndexOf(contents, 'v')), 'var_foo', 0)
    }

    @Test
    void testAssignedVariable4() {
        createDsld '''\
            contribute(bind(exprs: assignedVariable(name('foo')))) {
              property name: 'var_' + exprs[0].leftExpression.name
            }
            '''.stripIndent()

        String contents = '''\
            def foo = v
            '''.stripIndent()
        checkUniqueProposal(contents, 'v', 'var_foo')
    }

    @Test
    void testAssignedVariable4a() {
        createDsld '''\
            contribute(bind(exprs: assignedVariable(name('boo')))) {
              property name: 'var_' + exprs[0].leftExpression.name
            }
            '''.stripIndent()

        String contents = '''\
            def foo = v
            '''.stripIndent()
        proposalExists(createProposalsAtOffset(contents, getIndexOf(contents, 'v')), 'var_foo', 0)
    }

    @Test
    void testAssignedVariable5() {
        createDsld '''\
            contribute(bind(exprs: assignedVariable(type(BigInteger)))) {
              property name: 'var_' + exprs[0].leftExpression.name
            }
            '''.stripIndent()

        String contents = '''\
            BigInteger foo = v
            '''.stripIndent()
        checkUniqueProposal(contents, 'v', 'var_foo')
    }

    @Test
    void testAssignedVariable5a() {
        createDsld '''\
            contribute(bind(exprs: assignedVariable(type(BigInteger)))) {
              property name: 'var_' + exprs[0].leftExpression.name
            }
            '''.stripIndent()

        String contents = '''\
            def foo = v
            '''.stripIndent()
        proposalExists(createProposalsAtOffset(contents, getIndexOf(contents, 'v')), 'var_foo', 0)
    }

    @Test
    void testAssignedVariable6() {
        createDsld '''\
            contribute(bind(exprs: assignedVariable())) {
              property name: 'var_' + exprs[0].leftExpression.name
            }
            '''.stripIndent()

        String contents = 'def foo = '
        checkUniqueProposal(contents, '= ', 'var_foo')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/600
    void testAssignedVariable6a() {
        createDsld '''\
            contribute(bind(exprs: assignedVariable())) {
              property name: 'var_' + exprs[0].leftExpression.name
            }
            '''.stripIndent()

        String contents = 'foo = '.stripIndent()
        checkUniqueProposal(contents, '= ', 'var_foo')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/598
    void testAssignedVariable7() {
        createDsld '''\
            contribute(bind(exprs: assignedVariable())) {
              property name: 'var_' + exprs[0].leftExpression.name
            }
            '''.stripIndent()

        String contents = '''\
            def foo = { }
            '''.stripIndent()
        checkUniqueProposal(contents, '{ ', 'var_foo')
    }

    @Test
    void testAssignedVariable7a() {
        createDsld '''\
            contribute(bind(exprs: assignedVariable())) {
              property name: 'var_' + exprs[0].leftExpression.name
            }
            '''.stripIndent()

        String contents = '''\
            foo = { }
            '''.stripIndent()
        checkUniqueProposal(contents, '{ ', 'var_foo')
    }

    @Test
    void testAssignedVariable8() {
        createDsld '''\
            contribute(bind(exprs: assignedVariable())) {
              property name: 'var_' + exprs[0].leftExpression.name
            }
            '''.stripIndent()

        String contents = '''\
            def foo = {
              bar {
                baz {
                }
              }
            }
            '''.stripIndent()
        checkUniqueProposal(contents, 'baz {', 'var_foo')
    }

    @Test
    void testAssignedVariable8a() {
        createDsld '''\
            contribute(bind(exprs: assignedVariable())) {
              property name: 'var_' + exprs[0].leftExpression.name
            }
            '''.stripIndent()

        String contents = '''\
            foo = {
              bar {
                baz {
                }
              }
            }
            '''.stripIndent()
        checkUniqueProposal(contents, 'baz {', 'var_foo')
    }

    @Test
    void testDelegatesToNoParens1() {
        createDsld '''\
            contribute(currentType('Inner')) {
              delegatesTo type: 'Other', noParens: true
            }
            '''.stripIndent()

        String contents = '''\
            class Other {
              def blart(a, b, c) { }
              def flart(a) { }
            }
            class Inner { }
            def val = new Inner()
            val.bl
            '''.stripIndent()
        ICompletionProposal proposal = checkUniqueProposal(contents, 'val.bl', 'blart', 'blart val, val, val')
        applyProposalAndCheck(proposal, contents.replace('val.bl', 'val.blart val, val, val'))
    }

    @Test
    void testDelegatesToNoParens2() {
        createDsld '''\
            contribute(currentType('Inner')) {
              delegatesTo type: 'Other', noParens: true
            }
            '''.stripIndent()

        String contents = '''\
            class Other {
              def blart(a, b, c) { }
              def flart(a) { }
            }
            class Inner { }
            def val = new Inner()
            val.fl
            '''.stripIndent()
        ICompletionProposal proposal = checkUniqueProposal(contents, 'val.fl', 'flart', 'flart val')
        applyProposalAndCheck(proposal, contents.replace('val.fl', 'val.flart val'))
    }

    @Test // GRECLIPSE-1324
    void testEmptyClosure1() {
        createDsld '''\
            contribute(currentType(Integer) & enclosingCallName('foo')) {
              setDelegateType(String)
            }
            '''.stripIndent()

        String contents = '''\
            def foo(@DelegatesTo(Integer) Closure cl) {
            }
            foo {
              // here
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '\n  '))
        // should see proposals from String, not Integer
        proposalExists(proposals, 'substring', 2)
        proposalExists(proposals, 'bytes', 1)
        proposalExists(proposals, 'abs', 0)
        proposalExists(proposals, 'capitalize', 1)
        proposalExists(proposals, 'digits', 0)
    }

    @Test // GRECLIPSE-1324
    void testEmptyClosure2() {
        createDsld '''\
            contribute(currentType(Integer) & enclosingCallName('foo')) {
              setDelegateType(String)
            }
            '''.stripIndent()

        String contents = '''\
            def foo(@DelegatesTo(Integer) Closure cl) {
            }
            foo {
              to
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ' to') + 1)
        // should see proposals from String, not Integer
        proposalExists(proposals, 'toUpperCase()', 1)
        proposalExists(proposals, 'toHexString()', 0)
    }

    @Test
    void testCommandChain1() {
        createDsld '''\
            contribute(currentType('Inner')) {
              method name:'flart', noParens:true, type:'Inner'
            }
            '''.stripIndent()

        String contents = '''\
            class Inner { }
            def val = new Inner()
            val.fla
            '''.stripIndent()
        ICompletionProposal proposal = checkUniqueProposal(contents, '.fla', 'flart', 'flart()')
        applyProposalAndCheck(proposal, contents.replace('val.fla', 'val.flart()'))
    }

    @Test
    void testCommandChain2() {
        createDsld '''\
            contribute(currentType('Inner')) {
              method name:'flart', noParens:true, type:'Inner'
            }
            '''.stripIndent()

        String contents = '''\
            class Inner { }
            def val = new Inner()
            val.flart foo fl
            '''.stripIndent()
        ICompletionProposal proposal = checkUniqueProposal(contents, ' fl', 'flart', 'flart()')
        applyProposalAndCheck(proposal, contents.replace(' fl', ' flart()'))
    }

    @Test
    void testCommandChain3() {
        createDsld '''\
            contribute(currentType('Inner')) {
              method name:'flart', noParens:true, type:'Inner'
            }
            '''.stripIndent()

        String contents = '''\
            class Inner { }
            def val = new Inner()
            val.flart foo, baz fl
            '''.stripIndent()
        ICompletionProposal proposal = checkUniqueProposal(contents, ' fl', 'flart', 'flart()')
        applyProposalAndCheck(proposal, contents.replace(' fl', ' flart()'))
    }

    @Test
    void testCommandChain4() {
        createDsld '''\
            contribute(currentType('Inner')) {
              method name:'flart', noParens:true, type:'Inner', params:[a:Integer]
            }
            '''.stripIndent()

        String contents = '''\
            class Inner { }
            def val = new Inner()
            val.flart foo, baz fl
            '''.stripIndent()

        ICompletionProposal proposal = checkUniqueProposal(contents, ' fl', 'flart', 'flart 0')
        applyProposalAndCheck(proposal, contents.replace(' fl', ' flart 0'))
    }

    @Test
    void testCommandChain5() {
        createDsld '''\
            contribute(currentType('Inner')) {
              method name:'flart', noParens:true, type:'Inner', params:[a:Integer, b:String]
            }
            '''.stripIndent()

        String contents = '''\
            class Inner { }
            def val = new Inner()
            val.flart foo, baz fl
            '''.stripIndent()
        ICompletionProposal proposal = checkUniqueProposal(contents, ' fl', 'flart', 'flart 0, ""')
        applyProposalAndCheck(proposal, contents.replace(' fl', ' flart 0, ""'))
    }

    //--------------------------------------------------------------------------
    // Built-in contributions:

    @Test
    void testNewifyTransform1() {
        String contents = '''\
            @Newify class Foo {
              List list = ArrayList.n
              Map map = HashM
            }
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.n'))
        proposalExists(proposals, 'new', 3) // one for each constructor in ArrayList

        proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'HashM')))
        proposalExists(proposals, 'HashMap', 0)
    }

    @Test
    void testNewifyTransform2() {
        String contents = '''\
            @Newify(HashMap) class Foo {
              List list = ArrayList.n
              Map map = HashM
            }
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.n'))
        proposalExists(proposals, 'new', 3) // one for each constructor in ArrayList

        proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'HashM')))
        proposalExists(proposals, 'HashMap', 4) // one for each constructor in HashMap
    }

    @Test
    void testNewifyTransform3() {
        String contents = '''\
            @Newify(auto=false, value=HashMap) class Foo {
              List list = ArrayList.n
              Map map = HashM
            }
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.n'))
        proposalExists(proposals, 'new', 0)

        proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'HashM')))
        proposalExists(proposals, 'HashMap', 4) // one for each constructor in HashMap
    }

    @Test
    void testNewifyTransform4() {
        String contents = '''\
            @Newify
            List list = ArrayList.n
            @Newify(HashMap)
            Map map = HashM
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.n'))
        proposalExists(proposals, 'new', 3) // one for each constructor in ArrayList

        proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'HashM')))
        proposalExists(proposals, 'HashMap', 4) // one for each constructor in HashMap
    }

    @Test
    void testNewifyTransform5() {
        assumeTrue(isAtLeastGroovy(25)) // @Newify(pattern=...) added in Groovy 2.5

        String contents = '''\
            @Newify(auto=false, pattern=/(Linked)?Hash.*/) class Foo {
              List list = ArrayList.n
              Map map = HashM
            }
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.n'))
        proposalExists(proposals, 'new', 0)

        proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'HashM')))
        proposalExists(proposals, 'HashMap', 4) // one for each constructor in HashMap
    }

    @Test
    void testNewifyTransform5a() {
        assumeTrue(isAtLeastGroovy(25)) // @Newify(pattern=...) added in Groovy 2.5

        String contents = '''\
            @Newify(auto=false, pattern=/(Linked)?Hash.*/) class Foo {
              Map map = LinkedH
            }
            '''.stripIndent()

        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'LinkedH')))
        proposalExists(proposals, 'LinkedHashMap', 5) // one for each constructor in LinkedHashMap
    }

    @Test
    void testNewifyTransform5b() {
        assumeTrue(isAtLeastGroovy(25)) // @Newify(pattern=...) added in Groovy 2.5

        String contents = '''\
            @Newify(auto=false, pattern=/(Linked)?Hash.*/) class Foo {
              Map map = LinkedHashMap()
            }
            '''.stripIndent()

        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'LinkedHashMap')))
        proposalExists(proposals, 'LinkedHashMap', 5) // one for each constructor in LinkedHashMap
    }

    @Test
    void testSelfTypeTransform1() {
        String contents = '''\
            import groovy.transform.*

            class Foo { String string }

            @CompileStatic
            @SelfType(Foo)
            trait Bar {
              void baz() {
                def s1 = str
                def s2 = getStr
              }
            }
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'str'))
        proposalExists(proposals, 'string', 1)

        proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'getStr')))
        proposalExists(proposals, 'getString()', 1)
    }

    @Test
    void testSelfTypeTransform2() {
        String contents = '''\
            import groovy.transform.*

            class Foo { String string }

            @CompileStatic
            @SelfType([Foo, GroovyObject])
            trait Bar {
              void baz() {
                def s1 = str
                def s2 = getStr
              }
            }
            '''.stripIndent()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'str'))
        proposalExists(proposals, 'string', 1)

        proposals = orderByRelevance(createProposalsAtOffset(contents, getLastIndexOf(contents, 'getStr')))
        proposalExists(proposals, 'getString()', 1)
    }

    @Test
    void testSingletonTransform1() {
        String contents = '''\
            @Singleton class Foo { static Object ijk }
            Foo.i
            '''.stripIndent()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, '.i')))
        // contributed by built-in DLSD for @Singleton AST transform
        assertProposalOrdering(proposals, 'instance', 'ijk')
    }

    @Test
    void testSingletonTransform2() {
        String contents = '''\
            @Singleton class Foo { static Object getIjk() { } }
            Foo.g
            '''.stripIndent()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, '.g')))
        // contributed by built-in DLSD for @Singleton AST transform
        assertProposalOrdering(proposals, 'getInstance', 'getIjk')
    }

    @Test @Ignore('groovy-swing not included by default since 2.5')
    void testSwingBuilder1() {
        String contents = '''\
            import groovy.swing.SwingBuilder
            new SwingBuilder().edt {
              delegate.f
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'delegate.f')))
        // contributed by built-in DSLD for SwingBuilder
        assertProposalOrdering(proposals, 'frame', 'find')
    }

    @Test @Ignore('groovy-swing not included by default since 2.5')
    void testSwingBuilder2() {
        String contents = '''\
            import groovy.swing.SwingBuilder
            new SwingBuilder().edt {
              fr
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, 'fr')))
        // contributed by built-in DSLD for SwingBuilder
        assertProposalOrdering(proposals, 'frame', 'FrameFactory - groovy.swing.factory')
    }

    @Test @Ignore('groovy-swing not included by default since 2.5')
    void testSwingBuilder3() {
        String contents = '''\
            import groovy.swing.SwingBuilder
            new SwingBuilder().edt {
              this.x
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this.'))
        // proposals should not exist since not applied to 'this'
        proposalExists(proposals, 'frame', 0)
        proposalExists(proposals, 'registerBinding', 0)
    }
}
