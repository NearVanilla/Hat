package net.woek.Hat;
//TODO: Change package name to com.sigong.Hat

import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class HatHandler implements CommandExecutor, Listener {

    private final boolean messagesEnabled;
    private final String setMessage;
    private final String stackSizeMessage;
    private final String noPermissionMessage;
    private final String consoleMessage;
    private final String curseOfBindingMessage;

    private final Hat instance;

    //Constructor, Grabs messages gets messages from config file
    public HatHandler(Hat instance, boolean enabled, String set, String stacksize, String nopermission, String console, String curseMessage){

        this.instance = instance;
        messagesEnabled = enabled;
        setMessage = ChatColor.translateAlternateColorCodes('&',set);
        stackSizeMessage = ChatColor.translateAlternateColorCodes('&',stacksize);
        noPermissionMessage = ChatColor.translateAlternateColorCodes('&',nopermission);
        consoleMessage = ChatColor.translateAlternateColorCodes('&',console);
        curseOfBindingMessage = ChatColor.translateAlternateColorCodes('&', curseMessage);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        //Checks if sender is a player
        if(sender instanceof Player){
            Player player = (Player) sender;
            PlayerInventory inv = player.getInventory();

            ItemStack held = inv.getItemInMainHand();
            ItemStack helm = inv.getHelmet();

            //If the hat is valid and the player has permission to wear it
            if(checkValidHat(player, held)){
                //player.sendMessage("Best slot: " + held.getType().getEquipmentSlot());//TODO REMOVE
                inv.setHelmet(held);
                inv.setItemInMainHand(helm);
                player.updateInventory();
                if(messagesEnabled) {player.sendMessage(setMessage);}
            }
        }else{ //The command is being sent from the console
            sender.sendMessage(consoleMessage);
        }
        return true;
    }

    //Handles the player putting the hat in the slot manually
    //  A task is scheduled instead of doing everything in this method because I only want to run code if the item that was clicked with
    //  wouldn't normally be put into the helmet slot (this prevents checking permissions for helmets, pumpkin, etc)
    @EventHandler
    public void onClickInHelmetSlot(InventoryClickEvent event){
        if(event.getInventory().getType() == InventoryType.CRAFTING &&
                event.getRawSlot() == 5 &&
                event.getWhoClicked().getItemOnCursor().getType() != Material.AIR &&
                event.getWhoClicked().getItemOnCursor().getType().getEquipmentSlot() != EquipmentSlot.HEAD /*&&
                event.getWhoClicked().getInventory().getHelmet() == null*/){

            Player player = (Player) event.getWhoClicked();
            ItemStack cursorItem = player.getItemOnCursor(); //unknown if clone necessary

            ItemStack hatItem = player.getInventory().getHelmet(); //unknown if clone necessary

            if(checkValidHat(player, cursorItem)){
                player.setItemOnCursor(null);
                player.getInventory().setHelmet(null);
                (new oneTickRunnable(player, cursorItem, hatItem)).runTaskLater(instance, 1L);
            }
        }
    }

    //Delays hat placement by one tick (necessary for some reason)
    class oneTickRunnable extends BukkitRunnable{

        public oneTickRunnable(Player player, ItemStack cursorItem, ItemStack hatItem){
            this.player = player;
            this.cursorItem = cursorItem;

            this.hatItem = hatItem;
        }

        private final Player player;
        private final ItemStack cursorItem;

        private final ItemStack hatItem;

        @Override
        public void run() {
            player.setItemOnCursor(hatItem);
            player.getInventory().setHelmet(cursorItem);
            if(messagesEnabled){player.sendMessage(setMessage);}
        }
    }

    //Checks hat conditions (permission and itemstack) for both the command and manual placement
    //Returns true if the hat is valid (1 item), the player has permission to wear it and the players helmet slot does not have the curse of binding enchantment on it.
    //Returns false otherwise
    private boolean checkValidHat(Player player, ItemStack held){

        PlayerInventory inv = player.getInventory(); // Get inventory of player.
        ItemStack helmetSlot = inv.getHelmet(); // Get helmet slot of player.
        
        if(held.getType() == Material.AIR){ // If the item the player is holding is air.

            return false; // Invalid Case.
            
        } // End of if statement.

        if(player.hasPermission("hat." + held.getType().name())){ // If the player has the permission for the item they wish to put on their head. 

            Map<Enchantment, Integer> enchantments; // Create map for helmet enchantments.

            try{ // Enter try, as getting enchantments can produce an exception if the item does not exist.

                enchantments = helmetSlot.getEnchantments(); // Attempt to set enchantments to the helmet slots enchantments.

            }catch(Exception err){ // If error.

                enchantments = new HashMap<Enchantment, Integer>(); // Set enchantments to empty hashmap.

            } // End of try-catch.

            if(enchantments.containsKey(Enchantment.BINDING_CURSE)){ // If helmet has curse of binding.

                if(messagesEnabled){ // If messages are enabled.

                    player.sendMessage(curseOfBindingMessage); // Send curse error message.
                    return false; // Invalid case.

                } // End of if statement.

            } // End of if statement.

            if(held.getAmount() == 1){ // If amount of item player is holding is 1.

                return true; // Valid case.
                
            }else{ // In any other case.

                if(messagesEnabled){ // If messages enabled.

                    player.sendMessage(stackSizeMessage); // Send stack size error message.

                } // End of if statement.

            } // End of if statement.
            
        }else{ // In any other case.

            if(messagesEnabled){ // If messages are enabled.

                player.sendMessage(noPermissionMessage); // Send permission error message.

            } // End of if statement.

        } // End of if statement.
        
        return false; // Invalid case.
        
    } // End of function.

} // End of class.