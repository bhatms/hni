package org.hni.order.service;

import org.hni.order.dao.DefaultPartialOrderDAO;
import org.hni.order.dao.OrderDAO;
import org.hni.order.om.Order;
import org.hni.order.om.OrderItem;
import org.hni.order.om.PartialOrder;
import org.hni.order.om.TransactionPhase;
import org.hni.provider.om.GeoCodingException;
import org.hni.provider.om.Menu;
import org.hni.provider.om.MenuItem;
import org.hni.provider.om.Provider;
import org.hni.provider.om.ProviderLocation;
import org.hni.provider.service.MenuService;
import org.hni.provider.service.ProviderLocationService;
import org.hni.user.dao.UserDAO;
import org.hni.user.om.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test the OrderProcessorService.  This class assumes that anything consumed from within the app (from DAOs ect.) are 0 fault.
 *      These test wil ONLY be testing faults from consumer input.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test-applicationContext.xml"})
public class TestDefaultOrderProcessorService {

    @Mock
    private UserDAO userDAO;

    @Mock
    private DefaultPartialOrderDAO partialOrderDAO;

    @Mock
    private ProviderLocationService providerLocationService;

    @Mock
    private MenuService menuService;

    @Mock
    private OrderDAO orderDAO;

    @Inject
    @InjectMocks
    private DefaultOrderProcessor orderProcessor;

    private User user;
    private List<ProviderLocation> providerLocationList;
    private List<MenuItem> menuItems;

    private ArgumentCaptor<PartialOrder> argumentCaptor;
    private PartialOrder partialOrder;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        argumentCaptor = ArgumentCaptor.forClass(PartialOrder.class);
        partialOrder = new PartialOrder();
        providerLocationList = new ArrayList<>();
        menuItems = new ArrayList<>();

        standardTestData();
    }

    private void standardTestData() {
        user = new User("John", "Handcock", "1234567890");

        MenuItem item = new MenuItem("Food", "Good Food", 12.12, new Date());

        Menu menu = new Menu(1L);
        menu.setStartHourAvailable((long)0);
        menu.setEndHourAvailable((long)23);
        Set itemSet = new HashSet<>();
        itemSet.add(item);
        menu.setMenuItems(itemSet);

        Provider provider = new Provider(1L);
        Set menuSet = new HashSet<>();
        menuSet.add(menu);
        provider.setMenus(menuSet);

        ProviderLocation providerLocation = new ProviderLocation(1L);
        providerLocation.setName("Subway");
        providerLocation.setProvider(provider);
        menuItems.add(item);
        providerLocationList.add(providerLocation);

        providerLocation = new ProviderLocation(2L);
        providerLocation.setName("McDonalds");
        providerLocation.setProvider(provider);
        menuItems.add(item);
        providerLocationList.add(providerLocation);

        providerLocation = new ProviderLocation(3L);
        providerLocation.setName("Waffle House");
        providerLocation.setProvider(provider);
        menuItems.add(item);
        providerLocationList.add(providerLocation);
    }

    @Test
    public void processMessage_meal_success() {
        // Setup
        String message = "MEAL";
        Mockito.when(partialOrderDAO.byUser(user)).thenReturn(null);

        // Execute
        String output = orderProcessor.processMessage(user, message);
        String expectedOutput = "Please provide your address or ENDMEAL to quit";

        // Verify
        Assert.assertEquals(expectedOutput, output);
        ArgumentCaptor<PartialOrder> argumentCaptor = ArgumentCaptor.forClass(PartialOrder.class);
        Mockito.verify(partialOrderDAO, Mockito.times(1)).save(argumentCaptor.capture());
        Assert.assertEquals(TransactionPhase.PROVIDING_ADDRESS, argumentCaptor.getValue().getTransactionPhase());
    }

    @Test
    public void processMessage_providingAddress_success() {
        // Setup
        String message = "5540 S Hyde Park Blvd, Chicago IL, 60637";
        partialOrder.setTransactionPhase(TransactionPhase.PROVIDING_ADDRESS);

        Mockito.when(partialOrderDAO.byUser(user)).thenReturn(partialOrder);
        Mockito.when(providerLocationService.providersNearCustomer(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(providerLocationList);

        // Execute
        String output = orderProcessor.processMessage(user, message);
        String expectedOutput = "Please provide the number for your selection. "
                + "1) Subway (Food). "
                + "2) McDonalds (Food). "
                + "3) Waffle House (Food). ";

        // Verify
        Assert.assertEquals(expectedOutput, output);
        ArgumentCaptor<PartialOrder> argumentCaptor = ArgumentCaptor.forClass(PartialOrder.class);
        Mockito.verify(partialOrderDAO, Mockito.times(1)).save(argumentCaptor.capture());
        Assert.assertEquals(TransactionPhase.CHOOSING_LOCATION, argumentCaptor.getValue().getTransactionPhase());
        Assert.assertEquals(providerLocationList, argumentCaptor.getValue().getProviderLocationsForSelection());
    }

    @Test
    public void processMessage_providingAddress_badLocation() {
        // Setup
        String message = "5540 S Hyde Park Blvd, Chicago IL, 60637";
        partialOrder.setTransactionPhase(TransactionPhase.PROVIDING_ADDRESS);

        Mockito.when(partialOrderDAO.byUser(user)).thenReturn(partialOrder);
        Mockito.when(providerLocationService.providersNearCustomer(Mockito.anyString(), Mockito.anyInt()))
                .thenThrow(new GeoCodingException("Unable to resolve address"));

        // Execute
        String output = orderProcessor.processMessage(user, message);
        String expectedOutput = "Unable to resolve address";

        // Verify
        Assert.assertEquals(expectedOutput, output);
        ArgumentCaptor<PartialOrder> argumentCaptor = ArgumentCaptor.forClass(PartialOrder.class);
        Mockito.verify(partialOrderDAO, Mockito.times(1)).save(argumentCaptor.capture());
        Assert.assertEquals(TransactionPhase.PROVIDING_ADDRESS, argumentCaptor.getValue().getTransactionPhase());
    }

    @Test
    public void processMessage_choosingLocation_success() {
        // Setup
        String message = "2";
        partialOrder.setTransactionPhase(TransactionPhase.CHOOSING_LOCATION);
        partialOrder.setProviderLocationsForSelection(providerLocationList);
        partialOrder.setMenuItemsForSelection(menuItems);

        Mockito.when(partialOrderDAO.byUser(user)).thenReturn(partialOrder);

        // Execute
        String output = orderProcessor.processMessage(user, message);
        String expectedOutput = "You have chosen Food at McDonalds."
                + " Respond with CONFIRM to place this order, REDO to try again, or ENDMEAL to end your order";

        // Verify
        Assert.assertEquals(expectedOutput, output);
        ArgumentCaptor<PartialOrder> argumentCaptor = ArgumentCaptor.forClass(PartialOrder.class);
        Mockito.verify(partialOrderDAO, Mockito.times(1)).save(argumentCaptor.capture());
        Assert.assertEquals(TransactionPhase.CONFIRM_OR_CONTINUE, argumentCaptor.getValue().getTransactionPhase());
    }

    @Test
    public void processMessage_choosingLocation_badResponse() {
        // Setup
        String message = "4";
        partialOrder.setTransactionPhase(TransactionPhase.CHOOSING_LOCATION);
        partialOrder.setProviderLocationsForSelection(providerLocationList);
        partialOrder.setMenuItemsForSelection(menuItems);

        Mockito.when(partialOrderDAO.byUser(user)).thenReturn(partialOrder);

        // Execute
        String output = orderProcessor.processMessage(user, message);
        String expectedOutput = "Invalid input! "
                + "Please provide the number for your selection. "
                + "1) Subway (Food). "
                + "2) McDonalds (Food). "
                + "3) Waffle House (Food). ";

        // Verify
        Assert.assertEquals(expectedOutput, output);
        ArgumentCaptor<PartialOrder> argumentCaptor = ArgumentCaptor.forClass(PartialOrder.class);
        Mockito.verify(partialOrderDAO, Mockito.times(1)).save(argumentCaptor.capture());
        Assert.assertEquals(TransactionPhase.CHOOSING_LOCATION, argumentCaptor.getValue().getTransactionPhase());
    }

    @Test
    public void processMessage_confirmOrRedo_confirm() {
        // Setup
        String message = "CONFIRM";

        user.setId(1L);
        partialOrder.setUser(user);
        partialOrder.setTransactionPhase(TransactionPhase.CONFIRM_OR_CONTINUE);
        partialOrder.setProviderLocationsForSelection(providerLocationList);
        partialOrder.setMenuItemsForSelection(menuItems);
        partialOrder.setChosenProvider(providerLocationList.get(1));
        partialOrder.getMenuItemsSelected().add(menuItems.get(0));

        Mockito.when(partialOrderDAO.byUser(user)).thenReturn(partialOrder);
        Mockito.when(orderDAO.save(Mockito.any())).thenReturn(null);

        Date orderDate = new Date();
        // Execute
        String output = orderProcessor.processMessage(user, message);
        String expectedOutput = "Your order has been confirmed, please wait for more information about when to pick up your meal.";

        // Verify
        Assert.assertEquals(expectedOutput, output);
        Mockito.verify(partialOrderDAO, Mockito.times(1)).delete(partialOrder);
        ArgumentCaptor<Order> argumentCaptor = ArgumentCaptor.forClass(Order.class);
        Mockito.verify(orderDAO, Mockito.times(1)).save(argumentCaptor.capture());

        Assert.assertEquals(user.getId(), argumentCaptor.getValue().getUser().getId());
        Assert.assertTrue(argumentCaptor.getValue().getOrderDate().getTime() >= orderDate.getTime());
        Assert.assertEquals(partialOrder.getChosenProvider(), argumentCaptor.getValue().getProviderLocation());
        //Assert.assertEquals(partialOrder.getMenuItemsSelected(), argumentCaptor.getValue().getOrderItems());
        Assert.assertEquals(partialOrder.getMenuItemsSelected().iterator().next().getPrice(), argumentCaptor.getValue().getSubTotal());
    }

    @Test
    public void processMessage_confirmOrRedo_cancel() {
        // Setup
        String message = "endmeal";

        user.setId(1L);
        partialOrder.setUser(user);
        partialOrder.setTransactionPhase(TransactionPhase.CONFIRM_OR_CONTINUE);
        partialOrder.setProviderLocationsForSelection(providerLocationList);

        Mockito.when(partialOrderDAO.byUser(user)).thenReturn(partialOrder);
        Mockito.when(orderDAO.save(Mockito.any())).thenReturn(null);

        Date orderDate = new Date();
        // Execute
        String output = orderProcessor.processMessage(user, message);
        String expectedOutput = "You have successfully cancelled your order.";

        // Verify
        Assert.assertEquals(expectedOutput, output);
        Mockito.verify(partialOrderDAO, Mockito.times(1)).delete(partialOrder);
    }

    @Test
    public void processMessage_confirmOrRedo_redo() {
        // Setup
        String message = "REDO";

        user.setId(1L);
        partialOrder.setUser(user);
        partialOrder.setTransactionPhase(TransactionPhase.CONFIRM_OR_CONTINUE);
        partialOrder.setProviderLocationsForSelection(providerLocationList);
        partialOrder.setMenuItemsForSelection(menuItems);
        partialOrder.setAddress("home");
        partialOrder.setChosenProvider(providerLocationList.get(1));
        partialOrder.getMenuItemsSelected().add(menuItems.get(0));

        Mockito.when(partialOrderDAO.byUser(user)).thenReturn(partialOrder);
        Mockito.when(providerLocationService.providersNearCustomer(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(providerLocationList);
        Date orderDate = new Date();
        // Execute
        String output = orderProcessor.processMessage(user, message);
        String expectedOutput = "Please provide the number for your selection. "
                + "1) Subway (Food). "
                + "2) McDonalds (Food). "
                + "3) Waffle House (Food). ";
        // Verify
        Assert.assertEquals(expectedOutput, output);
        ArgumentCaptor<PartialOrder> argumentCaptor = ArgumentCaptor.forClass(PartialOrder.class);
        Mockito.verify(partialOrderDAO, Mockito.times(1)).save(argumentCaptor.capture());
        Assert.assertEquals(TransactionPhase.CHOOSING_LOCATION, argumentCaptor.getValue().getTransactionPhase());
    }

    @Test
    public void processMessage_confirmOrRedo_failure() {
        // Setup
        String message = "BadValue";

        user.setId(1L);
        partialOrder.setUser(user);
        partialOrder.setTransactionPhase(TransactionPhase.CONFIRM_OR_CONTINUE);
        partialOrder.setProviderLocationsForSelection(providerLocationList);
        partialOrder.setMenuItemsForSelection(menuItems);
        partialOrder.setChosenProvider(providerLocationList.get(1));
        partialOrder.getMenuItemsSelected().add(menuItems.get(0));

        Mockito.when(partialOrderDAO.byUser(user)).thenReturn(partialOrder);

        Date orderDate = new Date();
        // Execute
        String output = orderProcessor.processMessage(user, message);
        String expectedOutput = "Please respond with CONFIRM, REDO, or ENDMEAL";
        // Verify
        Assert.assertEquals(expectedOutput, output);
        ArgumentCaptor<PartialOrder> argumentCaptor = ArgumentCaptor.forClass(PartialOrder.class);
        Mockito.verify(partialOrderDAO, Mockito.times(0)).save(argumentCaptor.capture());
        Assert.assertEquals(TransactionPhase.CONFIRM_OR_CONTINUE, partialOrder.getTransactionPhase());
    }

    @Test
    public void processMessage_cancel_noOrder() {
        // Setup
        String message = "ENDMEAL";

        user.setId(1L);

        Mockito.when(partialOrderDAO.byUser(user)).thenReturn(null);

        // Execute
        String output = orderProcessor.processMessage(user, message);
        String expectedOutput = "You are not currently ordering, please respond with MEAL to place an order.";

        // Verify
        Assert.assertEquals(expectedOutput, output);
    }

    @Test
    public void processMessage_junk_noOrder() {
        // Setup
        String message = "(o.o)7";

        user.setId(1L);

        Mockito.when(partialOrderDAO.byUser(user)).thenReturn(null);

        // Execute
        String output = orderProcessor.processMessage(user, message);
        String expectedOutput = "I don't understand that, please say MEAL to request a meal.";

        // Verify
        Assert.assertEquals(expectedOutput, output);
    }

}
