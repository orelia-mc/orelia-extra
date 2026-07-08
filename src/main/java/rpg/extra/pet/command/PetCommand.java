package rpg.extra.pet.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rpg.extra.pet.model.PetDefinition;
import rpg.extra.pet.service.PetService;

import java.util.Map;
import java.util.Set;

/**
 * {@code /pet [list|buy <id>|summon [id]|dismiss]} (SOW PetModule).
 */
public final class PetCommand implements CommandExecutor {

    private final PetService petService;

    public PetCommand(PetService petService) {
        this.petService = petService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            listPets(sender, player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "buy" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /pet buy <id>");
                    return true;
                }
                report(sender, petService.unlock(player, args[1]));
            }
            case "summon" -> {
                String petId = args.length >= 2 ? args[1] : petService.getSelectedPetId(player.getUniqueId());
                if (petId == null) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /pet summon <id>");
                    return true;
                }
                report(sender, petService.summon(player, petId));
            }
            case "dismiss" -> report(sender, petService.dismiss(player));
            default -> sender.sendMessage(ChatColor.YELLOW + "Usage: /pet [list|buy <id>|summon [id]|dismiss]");
        }
        return true;
    }

    private void listPets(CommandSender sender, Player player) {
        Map<String, PetDefinition> all = petService.getAllPets();
        Set<String> unlocked = petService.getUnlockedPets(player.getUniqueId());
        if (all.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "利用可能なペットがありません。");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "ペット一覧:");
        all.values().forEach(pet -> {
            boolean owned = unlocked.contains(pet.getId());
            sender.sendMessage(ChatColor.GRAY + "- " + pet.getId() + " (" + pet.getName() + ") "
                    + (owned ? ChatColor.GREEN + "所持済み" : ChatColor.GOLD + (pet.getPrice() + "G")));
        });
    }

    private void report(CommandSender sender, PetService.ActionResult result) {
        if (result == PetService.ActionResult.OK) {
            sender.sendMessage(ChatColor.GREEN + "OK");
            return;
        }
        String message = switch (result) {
            case PET_NOT_FOUND -> "指定したペットは存在しません。";
            case ALREADY_UNLOCKED -> "既に所持しています。";
            case NOT_UNLOCKED -> "そのペットはまだ購入していません。";
            case INSUFFICIENT_FUNDS -> "所持金が足りません。";
            case NO_ACTIVE_PET -> "呼び出し中のペットがいません。";
            case OK -> "OK";
        };
        sender.sendMessage(ChatColor.RED + message);
    }
}
