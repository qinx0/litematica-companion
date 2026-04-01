package qinx.litematicacompanion.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(BlockListWidget.class);

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
            // Try to auto-refresh the material list from active placements
            fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager manager =
                    fi.dy.masa.litematica.data.DataManager.getSchematicPlacementManager();

            if (manager == null || manager.getAllSchematicsPlacements().isEmpty()) {
                blockList.add("§cNo schematic loaded");
                return;
            }

            fi.dy.masa.litematica.schematic.placement.SchematicPlacement placement =
                    manager.getAllSchematicsPlacements().get(0);

            fi.dy.masa.litematica.materials.MaterialListSchematic materialList =
                    new fi.dy.masa.litematica.materials.MaterialListSchematic(
                            placement.getSchematic(),
                            placement.getSchematic().getAreas().keySet(),
                            true
                    );

            List<fi.dy.masa.litematica.materials.MaterialListEntry> entries =
                    materialList.getMaterialsAll();

            if (entries == null || entries.isEmpty()) {
                blockList.add("§cMaterial list is empty");
                return;
            }

            for (fi.dy.masa.litematica.materials.MaterialListEntry entry : entries) {
                String name = entry.getStack().getName().getString();
                long total = entry.getCountTotal();
                long missing = entry.getCountMissing();
                blockList.add("§f" + name);
                blockList.add("  §7Need: §e" + total + " §cMissing: " + missing);
            }

        } catch (Exception e) {
            blockList.add("§cError: " + e.getMessage());
        }
    }

    private float renderScale = 1.0f;

    public void setRenderScale(float scale) {
        this.renderScale = scale;
        log.info("New scale is " + scale);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        var stack = context.getMatrices();
        stack.pushMatrix();

        // Move to the top-left corner of the widget
        stack.translate((float)getX(), (float)getY());

        // Scale BOTH width and height axes
        stack.scale(renderScale, renderScale);

        // Background (Starts at 0, 0)
        context.fill(0, 0, this.width, this.height, 0xCC000000);

        // Border
        context.fill(0, 0, this.width, 1, 0xFFFFFFFF); // Top
        context.fill(0, 0, 1, this.height, 0xFFFFFFFF); // Left
        context.fill(this.width - 1, 0, this.width, this.height, 0xFFFFFFFF); // Right
        context.fill(0, this.height - 1, this.width, this.height, 0xFFFFFFFF); // Bottom

        DrawnTextConsumer consumer = context.getTextConsumer();

        // Title (4 pixels in from the new 0,0)
        consumer.text(4, 4, Text.literal("§fBlock List"));

        // Divider
        context.fill(0, 14, this.width, 15, 0xFFAAAAAA);

        // Entries
        int maxVisible = (this.height - 28) / ITEM_HEIGHT;
        for (int i = scrollOffset; i < Math.min(scrollOffset + maxVisible, blockList.size()); i++) {
            consumer.text(
                    4,
                    18 + (i - scrollOffset) * ITEM_HEIGHT,
                    Text.literal(blockList.get(i))
            );
        }

        // Bottom section
        context.fill(0, this.height - 12, this.width, this.height - 11, 0xFFAAAAAA);
        consumer.text(4, this.height - 10, Text.literal("§8scroll to see more"));

        stack.popMatrix();

    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        // The "Hitbox" must be shrunk to match the visual size
        double scaledWidth = this.width * renderScale;
        double scaledHeight = this.height * renderScale;

        return mouseX >= this.getX() && mouseX <= this.getX() + scaledWidth &&
                mouseY >= this.getY() && mouseY <= this.getY() + scaledHeight;
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