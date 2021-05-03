package org.apereo.portal.portlets.favorites;

import java.util.*;
import junit.framework.TestCase;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription;
import org.apereo.portal.layout.node.UserLayoutChannelDescription;
import org.junit.Assert;

public class FavoritesUtilsTest extends TestCase {

    public void testFilterFavoritesToUnique() {

        List<IUserLayoutNodeDescription> favArrayWithDuplicate = new ArrayList<>();
        List<IUserLayoutNodeDescription> favArray = new ArrayList<>();
        List<IUserLayoutNodeDescription> emptyArray = new ArrayList<>();

        UserLayoutChannelDescription node1 = new UserLayoutChannelDescription();
        node1.setId("n18");
        node1.setName("Cache Manager");
        node1.setPrecedence(0.0);
        node1.setMoveAllowed(true);
        node1.setUnremovable(false);
        node1.setDeleteAllowed(true);
        node1.setImmutable(false);
        node1.setEditAllowed(true);

        UserLayoutChannelDescription node2 = new UserLayoutChannelDescription();
        node2.setId("n28");
        node2.setName("Permissions Administration");
        node2.setPrecedence(0.0);
        node2.setMoveAllowed(true);
        node2.setUnremovable(false);
        node2.setDeleteAllowed(true);
        node2.setImmutable(false);
        node2.setEditAllowed(true);

        UserLayoutChannelDescription node3 = new UserLayoutChannelDescription();
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

        List<IUserLayoutNodeDescription> uniqueFavorites =
                FavoritesUtils.filterFavoritesToUnique(favArray);
        List<IUserLayoutNodeDescription> favoritesReturnedFromUniqueFavoritesFilter =
                FavoritesUtils.filterFavoritesToUnique(favArray);
        List<IUserLayoutNodeDescription> emptyFavoritesListReturnedFromFavoritesFilter =
                FavoritesUtils.filterFavoritesToUnique(emptyArray);

        Assert.assertEquals(3, favArray.size());

        // Confirms a list which is already unique will return the same
        // list
        Assert.assertEquals(3, favoritesReturnedFromUniqueFavoritesFilter.size());

        // Confirms duplicate array contains 4 items, first two items
        // being duplicates.
        Assert.assertEquals(4, favArrayWithDuplicate.size());

        // Confirms the new count of 3 after passing duplicate favorite
        // list to filterFavoritesToUnique method
        Assert.assertEquals(3, uniqueFavorites.size());

        // Gets id of second element in duplicate array which before
        // removing duplicate was "n18" and after it should be "n28"
        String ObjectIdOfSecondItem = uniqueFavorites.get(1).getId();

        // Confirms the first duplicate item was removed from the
        // list
        Assert.assertEquals("n28", ObjectIdOfSecondItem);

        Assert.assertEquals(0, emptyArray.size());
        Assert.assertEquals(0, emptyFavoritesListReturnedFromFavoritesFilter.size());
    }
}
