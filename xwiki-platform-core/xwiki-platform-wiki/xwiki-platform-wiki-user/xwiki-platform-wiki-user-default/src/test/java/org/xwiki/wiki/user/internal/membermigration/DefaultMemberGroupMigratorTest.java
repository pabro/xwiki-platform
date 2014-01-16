/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.wiki.user.internal.membermigration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.query.QueryFilter;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;
import org.xwiki.wiki.user.WikiUserManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultMemberGroupMigratorTest
{
    @Rule
    public MockitoComponentMockingRule<DefaultMemberGroupMigrator> mocker =
            new MockitoComponentMockingRule(DefaultMemberGroupMigrator.class);

    private WikiDescriptorManager wikiDescriptorManager;

    private WikiUserManager wikiUserManager;

    private DocumentReferenceResolver<String> documentReferenceResolver;

    private Provider<XWikiContext> xcontextProvider;

    private XWikiContext xcontext;

    private XWiki xwiki;

    @Before
    public void setUp() throws Exception
    {
        wikiDescriptorManager = mocker.getInstance(WikiDescriptorManager.class);
        wikiUserManager = mocker.getInstance(WikiUserManager.class);
        documentReferenceResolver = mocker.getInstance(new DefaultParameterizedType(null,
                DocumentReferenceResolver.class, String.class));
        xcontextProvider = mocker.getInstance(new DefaultParameterizedType(null, Provider.class, XWikiContext.class));

        mocker.registerMockComponent(QueryFilter.class, "unique");

        when(wikiDescriptorManager.getCurrentWikiId()).thenReturn("subwiki");
        when(wikiDescriptorManager.getMainWikiId()).thenReturn("xwiki");

        xcontext = mock(XWikiContext.class);
        xwiki = mock(XWiki.class);

        when(xcontextProvider.get()).thenReturn(xcontext);
        when(xcontext.getWiki()).thenReturn(xwiki);
    }

    @Test
    public void migrateGroups() throws Exception
    {
        String userLocal1 = "XWiki.UserLocal1";
        String userLocal2 = "XWiki.UserLocal2";
        String userGlobal1 = "xwiki:XWiki.UserGlobal1";
        String userGlobal2 = "xwiki:XWiki.UserGlobal2";

        DocumentReference userLocalRef1 = new DocumentReference("subwiki", "XWiki", "UserLocal1");
        DocumentReference userLocalRef2 = new DocumentReference("subwiki", "XWiki", "UserLocal2");
        DocumentReference userGlobalRef1 = new DocumentReference("xwiki", "XWiki", "UserGlobal1");
        DocumentReference userGlobalRef2 = new DocumentReference("xwiki", "XWiki", "UserGlobal2");

        BaseObject member1 = mock(BaseObject.class);
        when(member1.getStringValue("member")).thenReturn(userLocal1);
        BaseObject member2 = mock(BaseObject.class);
        when(member2.getStringValue("member")).thenReturn(userLocal2);
        BaseObject member3 = mock(BaseObject.class);
        when(member3.getStringValue("member")).thenReturn(userGlobal1);
        BaseObject member4 = mock(BaseObject.class);
        when(member4.getStringValue("member")).thenReturn(userGlobal2);
        List<BaseObject> membersOfAllGroup = new ArrayList<BaseObject>();
        membersOfAllGroup.add(member1);
        membersOfAllGroup.add(null);
        membersOfAllGroup.add(member2);
        membersOfAllGroup.add(member3);
        membersOfAllGroup.add(member4);

        when(documentReferenceResolver.resolve(userLocal1)).thenReturn(userLocalRef1);
        when(documentReferenceResolver.resolve(userLocal2)).thenReturn(userLocalRef2);
        when(documentReferenceResolver.resolve(userGlobal1)).thenReturn(userGlobalRef1);
        when(documentReferenceResolver.resolve(userGlobal2)).thenReturn(userGlobalRef2);

        DocumentReference allGroupRef = new DocumentReference("subwiki", "XWiki", "XWikiAllGroup");
        DocumentReference memberGroupRef = new DocumentReference("subwiki", "XWiki", "XWikiMemberGroup");

        XWikiDocument allGroupDoc = mock(XWikiDocument.class);
        XWikiDocument memberGroupDoc = mock(XWikiDocument.class);

        when(xwiki.getDocument(allGroupRef, xcontext)).thenReturn(allGroupDoc);
        when(xwiki.getDocument(memberGroupRef, xcontext)).thenReturn(memberGroupDoc);

        DocumentReference memberClass = new DocumentReference("subwiki", "XWiki", "XWikiGroups");
        when(allGroupDoc.getXObjects(eq(memberClass))).thenReturn(membersOfAllGroup);

        // Test
        mocker.getComponentUnderTest().migrateGroups("subwiki");

        // Verify
        List<String> expectedMembers = new ArrayList<String>();
        expectedMembers.add(userGlobal1);
        expectedMembers.add(userGlobal2);

        // Only global users has been added in the member list
        verify(wikiUserManager).addMembers(eq(expectedMembers), eq("subwiki"));
        // Ony Global users have been removed from XWikiAllGroup
        verify(allGroupDoc).removeXObject(member3);
        verify(allGroupDoc).removeXObject(member4);
        // But the local users have not been removed
        verify(allGroupDoc, never()).removeXObject(member1);
        verify(allGroupDoc, never()).removeXObject(member2);
        // The document has been saved
        verify(xwiki).saveDocument(allGroupDoc, "Remove all global users from this group.", xcontext);
    }
}