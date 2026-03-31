package qinx.litematicacompanion.gui;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.DrawnTextConsumer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BlockListWidget extends ClickableWidget {
    MinecraftClient client = MinecraftClient.getInstance();
    TextRenderer textRenderer = client.textRenderer;
    private final List<String> blockList = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int ITEM_HEIGHT = 12;

    public BlockListWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
        refreshBlockList();
        this.width = calculateWidth();
    }

    private int calculateWidth() {
        int minWidth = 120;
        int maxTextWidth = 0;

        // Check title
        maxTextWidth = Math.max(maxTextWidth, textRenderer.getWidth(Text.literal("§fBlock List")));

        // Check all entries
        for (String line : blockList) {
            maxTextWidth = Math.max(maxTextWidth, textRenderer.getWidth(Text.literal(line)));
        }

        // Check scroll hint
        maxTextWidth = Math.max(maxTextWidth, textRenderer.getWidth(Text.literal("§8scroll to see more")));

        // 4 units padding on each side (left +4, right +4)
        int neededWidth = maxTextWidth + 8;

        return Math.max(minWidth, neededWidth);
    }

    public void refreshBlockList() {
        blockList.clear();

        try {
            MaterialListBase materialList = DataManager.getMaterialList();

            if (materialList == null) {
                blockList.add("§cNo schematic loaded");
                return;
            }

            List<MaterialListEntry> entries = materialList.getMaterialsAll();

            if (entries == null || entries.isEmpty()) {
                blockList.add("§cMaterial list is empty");
                blockList.add("§7Open Litematica and");
                blockList.add("§7refresh the material list");
                return;
            }

            for (MaterialListEntry entry : entries) {
                String name = entry.getStack().getName().getString();
                long total = entry.getCountTotal();
                long missing = entry.getCountMissing();
                blockList.add("§f" + name);
                blockList.add("  §7Total: §e" + total + " §7Missing: §c" + missing);
            }

        } catch (Exception e) {
            blockList.add("§cLitematica not found");
        }
        this.width = calculateWidth();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        //  Background
        context.fill(getX(), getY(), getX() + this.width, getY() + this.height, 0xCC000000);

        // Border
        context.fill(getX(), getY(), getX() + this.width, getY() + 1, 0xFFFFFFFF);
        context.fill(getX(), getY(), getX() + 1, getY() + this.height, 0xFFFFFFFF);
        context.fill(getX() + this.width - 1, getY(), getX() + this.width, getY() + this.height, 0xFFFFFFFF);
        context.fill(getX(), getY() + this.height - 1, getX() + this.width, getY() + this.height, 0xFFFFFFFF);

        DrawnTextConsumer consumer = context.getTextConsumer();

        // Title
        consumer.text(getX() + 4, getY() + 4, Text.literal("§fBlock List"));

        // Divider
        context.fill(getX(), getY() + 14, getX() + this.width, getY() + 15, 0xFFAAAAAA);

        // Entries
        int maxVisible = (this.height - 28) / ITEM_HEIGHT;
        for (int i = scrollOffset; i < Math.min(scrollOffset + maxVisible, blockList.size()); i++) {
            consumer.text(
                    getX() + 4,
                    getY() + 18 + (i - scrollOffset) * ITEM_HEIGHT,
                    Text.literal(blockList.get(i))
            );
        }

        // Divider + scroll hint
        context.fill(getX(), getY() + this.height - 12, getX() + this.width, getY() + this.height - 11, 0xFFAAAAAA);
        consumer.text(getX() + 4, getY() + this.height - 10, Text.literal("§8scroll to see more"));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!isMouseOver(mouseX, mouseY)) return false;
        int maxVisible = (this.height - 28) / ITEM_HEIGHT;
        scrollOffset -= (int) verticalAmount;
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, blockList.size() - maxVisible)));
        return true;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}