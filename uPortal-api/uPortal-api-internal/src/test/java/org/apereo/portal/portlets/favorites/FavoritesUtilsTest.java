package org.apereo.portal.portlets.favorites;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.apereo.portal.layout.node.IUserLayoutChannelDescription;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription;
import org.apereo.portal.layout.node.UserLayoutChannelDescription;
import org.junit.Assert;

public class FavoritesUtilsTest extends TestCase {

    private List<IUserLayoutChannelDescription> favArrayWithDuplicate = new ArrayList<>();
    private List<IUserLayoutChannelDescription> favArray = new ArrayList<>();
    private List<IUserLayoutChannelDescription> emptyArray = new ArrayList<>();

    public void testFilterChannelFavoritesToUnique() {

        List<IUserLayoutChannelDescription> uniqueFavorites =
                FavoritesUtils.filterChannelFavoritesToUnique(favArray);
        List<IUserLayoutChannelDescription> favoritesReturnedFromUniqueFavoritesFilter =
                FavoritesUtils.filterChannelFavoritesToUnique(favArray);
        List<IUserLayoutChannelDescription> emptyFavoritesListReturnedFromFavoritesFilter =
                FavoritesUtils.filterChannelFavoritesToUnique(emptyArray);

        Assert.assertEquals(3, favArray.size());

        // Confirms a list which is already unique will return the same list
        Assert.assertEquals(3, favoritesReturnedFromUniqueFavoritesFilter.size());

        // Confirms duplicate array contains 4 items, first two items being duplicates.
        Assert.assertEquals(4, favArrayWithDuplicate.size());

        // Confirms the new count of 3 after passing duplicate favorite list to
        // filterFavoritesToUnique method
        Assert.assertEquals(3, uniqueFavorites.size());

        // Gets id of second element in duplicate array which before
        // removing duplicate was "n18" and after it should be "n28"
        String ObjectIdOfSecondItem = uniqueFavorites.get(1).getId();
        Assert.assertEquals("n28", ObjectIdOfSecondItem);

        Assert.assertEquals(0, emptyArray.size());
        Assert.assertEquals(0, emptyFavoritesListReturnedFromFavoritesFilter.size());
    }

    public void testCastListChannelDescriptionToListNodeDescription() {

        List<IUserLayoutNodeDescription> castFavArray =
                FavoritesUtils.castListChannelDescriptionToListNodeDescription(favArray);
        List<IUserLayoutNodeDescription> castFavArrayWithDuplicate =
                FavoritesUtils.castListChannelDescriptionToListNodeDescription(
                        favArrayWithDuplicate);
        List<IUserLayoutNodeDescription> castEmptyArray =
                FavoritesUtils.castListChannelDescriptionToListNodeDescription(emptyArray);

        // Confirms 3 items in list which is already unique are returned
        Assert.assertEquals(3, castFavArray.size());

        // Confirms all 4 items in Duplicate list are return
        // its not the responsibility of this method to create a unique list, only to cast
        Assert.assertEquals(4, castFavArrayWithDuplicate.size());

        // Confirms if an empty list is sent it will return an empty list of the needed type
        Assert.assertEquals(0, castEmptyArray.size());

        // Confirms the correct type is returned
        Assert.assertTrue(castFavArray.get(0) instanceof IUserLayoutNodeDescription);
        Assert.assertTrue(castFavArrayWithDuplicate.get(0) instanceof IUserLayoutNodeDescription);
    }

    public void testGetDuplicateFavoritesByFNameToDelete() {

        List<IUserLayoutNodeDescription> emptyNodeArray = new ArrayList<>();

        List<IUserLayoutNodeDescription> testArray =
                FavoritesUtils.castListChannelDescriptionToListNodeDescription(favArray);
        List<IUserLayoutNodeDescription> testArrayWithDuplicate =
                FavoritesUtils.castListChannelDescriptionToListNodeDescription(
                        favArrayWithDuplicate);

        List<IUserLayoutChannelDescription> findOneNode =
                FavoritesUtils.getDuplicateFavoritesByFNameToDelete(testArray, "cache-manager");
        List<IUserLayoutChannelDescription> findTwoNodes =
                FavoritesUtils.getDuplicateFavoritesByFNameToDelete(
                        testArrayWithDuplicate, "cache-manager");
        List<IUserLayoutChannelDescription> findNodesNoFname =
                FavoritesUtils.getDuplicateFavoritesByFNameToDelete(testArrayWithDuplicate, "");
        List<IUserLayoutChannelDescription> findNodeEmptyFavList =
                FavoritesUtils.getDuplicateFavoritesByFNameToDelete(
                        emptyNodeArray, "cache-manager");
        List<IUserLayoutChannelDescription> findNodeEmptyFavListEmptyFname =
                FavoritesUtils.getDuplicateFavoritesByFNameToDelete(emptyNodeArray, "");

        // Confirms a list of Favorite nodes which has a duplicate node FName will return a list of
        // both nodes
        Assert.assertEquals(findTwoNodes.size(), 2);

        // Confirms a list of Favorite nodes which only has one node matching the provided FName
        // will return a list of 1
        Assert.assertEquals(findOneNode.size(), 1);

        // Confirms a list of favorites with an FName not provided will return 0
        Assert.assertEquals(findNodesNoFname.size(), 0);

        // Confirm an empty list of favorites will return 0 when queried for an FName
        Assert.assertEquals(findNodeEmptyFavList.size(), 0);

        // Confirms an empty list of favorites when queried for an empty string FName will also
        // return 0
        Assert.assertEquals(findNodeEmptyFavListEmptyFname.size(), 0);
    }

    @Override
    public void tearDown() throws Exception {
        favArrayWithDuplicate = null;
        favArray = null;
        emptyArray = null;
    }

    @Override
    public void setUp() throws Exception {

        UserLayoutChannelDescription node1 = new UserLayoutChannelDescription();
        node1.setFunctionalName("cache-manager");
        node1.setId("n18");
        node1.setName("Cache Manager");
        node1.setPrecedence(0.0);
        node1.setMoveAllowed(true);
        node1.setUnremovable(false);
        node1.setDeleteAllowed(true);
        node1.setImmutable(false);
        node1.setEditAllowed(true);

        UserLayoutChannelDescription node2 = new UserLayoutChannelDescription();
        node2.setFunctionalName("permissions-administration");
        node2.setId("n28");
        node2.setName("Permissions Administration");
        node2.setPrecedence(0.0);
        node2.setMoveAllowed(true);
        node2.setUnremovable(false);
        node2.setDeleteAllowed(true);
        node2.setImmutable(false);
        node2.setEditAllowed(true);

        UserLayoutChannelDescription node3 = new UserLayoutChannelDescription();
        node3.setFunctionalName("user-administration");
        node3.setId("n33");
        node3.setName("User Administration");
        node3.setPrecedence(0.0);
        node3.setMoveAllowed(true);
        node3.setUnremovable(false);
        node3.setDeleteAllowed(true);
        node3.setImmutable(false);
        node3.setEditAllowed(true);

        favArray.add(node1);
        favArray.add(node2);
        favArray.add(node3);

        // Add the first node from favArray to favArrayWithDuplicates which creates the duplicate
        // 'favorite' object in this Arraylist
        favArrayWithDuplicate.add(favArray.get(0));

        for (int i = 0; i < favArray.size(); i++) {
            favArrayWithDuplicate.add(favArray.get(i));
        }
    }
}
