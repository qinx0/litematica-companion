package qinx.litematicacompanion.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.DrawnTextConsumer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BlockListWidget extends ClickableWidget {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer;
    private final List<String> blockList = new ArrayList<>();
    private final List<fi.dy.masa.litematica.schematic.placement.SchematicPlacement> placements = new ArrayList<>();
    private int selectedPlacementIndex = 0;
    private int scrollOffset = 0;
    private static final int ITEM_HEIGHT = 12;
    private static final Logger log = LoggerFactory.getLogger(BlockListWidget.class);

    private boolean dropdownOpen = false;
    private static final int DROPDOWN_HEIGHT = 14;
    private static final int TITLE_HEIGHT = 14;
    private static final int FOOTER_HEIGHT = 12;

    public BlockListWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
        this.textRenderer = client.textRenderer;
        refreshBlockList();
        this.width = calculateWidth();
    }

    private int calculateWidth() {
        int minWidth = 120;
        int maxTextWidth = 0;

        maxTextWidth = Math.max(maxTextWidth, textRenderer.getWidth(Text.literal("§fBlock List")));
        maxTextWidth = Math.max(maxTextWidth, textRenderer.getWidth(getDropdownText()));

        for (String line : blockList) {
            maxTextWidth = Math.max(maxTextWidth, textRenderer.getWidth(Text.literal(line)));
        }

        maxTextWidth = Math.max(maxTextWidth, textRenderer.getWidth(Text.literal("§8scroll to see more")));

        int neededWidth = maxTextWidth + 8;

        return Math.max(minWidth, neededWidth);
    }

    private Text getDropdownText() {
        if (placements.isEmpty()) {
            return Text.literal("§7No placements");
        }
        String name = placements.get(selectedPlacementIndex).getSchematic().getMetadata().getName();
        String displayName = name.isEmpty() ? "Unnamed Schematic" : name;
        return Text.literal(dropdownOpen ? "§f▼ " + displayName : "§f▶ " + displayName);
    }

    public void refreshBlockList() {
        blockList.clear();
        placements.clear();
        selectedPlacementIndex = 0;
        scrollOffset = 0;

        try {
            fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager manager =
                    fi.dy.masa.litematica.data.DataManager.getSchematicPlacementManager();

            if (manager == null || manager.getAllSchematicsPlacements().isEmpty()) {
                blockList.add("§cNo schematic loaded");
                return;
            }

            placements.addAll(manager.getAllSchematicsPlacements());
            loadMaterialsForCurrentPlacement();

        } catch (Exception e) {
            log.error("Failed to load schematic placements", e);
            blockList.add("§cFailed to load materials");
            blockList.add("§7Check logs for details");
        }
    }

    private void loadMaterialsForCurrentPlacement() {
        blockList.clear();
        scrollOffset = 0;

        if (placements.isEmpty() || selectedPlacementIndex >= placements.size()) {
            blockList.add("§cNo placement selected");
            return;
        }

        try {
            fi.dy.masa.litematica.schematic.placement.SchematicPlacement placement =
                    placements.get(selectedPlacementIndex);

            if (placement.getSchematic() == null) {
                blockList.add("§cSchematic is null");
                return;
            }

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
            log.error("Failed to load materials for placement " + selectedPlacementIndex, e);
            blockList.add("§cFailed to load materials");
            blockList.add("§7Check logs for details");
        }
    }

    private float renderScale = 1.0f;
    private int opacity = 0xCC;

    public void setRenderScale(float scale) {
        this.renderScale = scale;
    }

    public void setOpacity(int opacity) {
        this.opacity = (opacity << 24) | 0x000000;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        var stack = context.getMatrices();
        stack.pushMatrix();

        stack.translate((float)getX(), (float)getY());
        stack.scale(renderScale, renderScale);

        int contentHeight = dropdownOpen ? TITLE_HEIGHT + DROPDOWN_HEIGHT + (this.height - TITLE_HEIGHT - FOOTER_HEIGHT) : this.height;

        context.fill(0, 0, this.width, contentHeight, opacity);

        context.fill(0, 0, this.width, 1, 0xFFFFFFFF);
        context.fill(0, 0, 1, contentHeight, 0xFFFFFFFF);
        context.fill(this.width - 1, 0, this.width, contentHeight, 0xFFFFFFFF);
        context.fill(0, contentHeight - 1, this.width, contentHeight, 0xFFFFFFFF);

        DrawnTextConsumer consumer = context.getTextConsumer();

        consumer.text(4, 4, Text.literal("§fBlock List"));

        context.fill(0, TITLE_HEIGHT, this.width, TITLE_HEIGHT + 1, 0xFFAAAAAA);

        int listTop = TITLE_HEIGHT + 1;
        int listBottom;

        if (dropdownOpen && placements.size() > 1) {
            int dropdownBg = (opacity & 0xFF000000) >> 2;
            context.fill(0, listTop, this.width, listTop + DROPDOWN_HEIGHT, dropdownBg | 0x000000);
            consumer.text(4, listTop + 3, getDropdownText());

            listTop += DROPDOWN_HEIGHT;

            int dropdownListTop = listTop;
            int dropdownListHeight = Math.min(placements.size() * ITEM_HEIGHT, this.height - TITLE_HEIGHT - FOOTER_HEIGHT - DROPDOWN_HEIGHT);

            for (int i = 0; i < placements.size(); i++) {
                if (i * ITEM_HEIGHT >= dropdownListHeight) break;
                String name = placements.get(i).getSchematic().getMetadata().getName();
                String displayName = name.isEmpty() ? "Unnamed" : name;
                Text itemText = Text.literal(i == selectedPlacementIndex ? "§e• " + displayName : "§7  " + displayName);
                consumer.text(4, dropdownListTop + i * ITEM_HEIGHT, itemText);
            }

            listTop += dropdownListHeight;
        } else {
            consumer.text(4, listTop + 3, getDropdownText());
        }

        int entriesTop;
        int entriesBottom = contentHeight - FOOTER_HEIGHT - 1;

        if (dropdownOpen) {
            int dropdownItemsHeight = Math.min(placements.size() * ITEM_HEIGHT, this.height - TITLE_HEIGHT - FOOTER_HEIGHT - DROPDOWN_HEIGHT);
            entriesTop = TITLE_HEIGHT + 1 + DROPDOWN_HEIGHT + dropdownItemsHeight;
        } else {
            entriesTop = TITLE_HEIGHT + 1 + DROPDOWN_HEIGHT;
        }

        context.fill(0, entriesTop, this.width, entriesTop + 1, 0xFFAAAAAA);

        if (!dropdownOpen) {
            int maxVisible = (entriesBottom - entriesTop - 4) / ITEM_HEIGHT;
            for (int i = scrollOffset; i < Math.min(scrollOffset + maxVisible, blockList.size()); i++) {
                consumer.text(4, entriesTop + 4 + (i - scrollOffset) * ITEM_HEIGHT, Text.literal(blockList.get(i)));
            }

            context.fill(0, entriesBottom, this.width, entriesBottom + 1, 0xFFAAAAAA);
            consumer.text(4, entriesBottom + 2, Text.literal("§8scroll to see more"));
        }

        stack.popMatrix();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        double scaledWidth = this.width * renderScale;
        double scaledHeight = this.height * renderScale;

        return mouseX >= this.getX() && mouseX <= this.getX() + scaledWidth &&
                mouseY >= this.getY() && mouseY <= this.getY() + scaledHeight;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        if (dropdownOpen) {
            return true;
        }

        int entriesTop = TITLE_HEIGHT + DROPDOWN_HEIGHT;
        int entriesBottom = this.height - FOOTER_HEIGHT - 1;
        int maxVisible = (entriesBottom - entriesTop - 4) / ITEM_HEIGHT;

        scrollOffset -= (int) verticalAmount;
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, blockList.size() - maxVisible)));
        return true;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        
        if (!isMouseOver(mouseX, mouseY)) return false;

        double scaledX = (mouseX - getX()) / renderScale;
        double scaledY = (mouseY - getY()) / renderScale;

        if (scaledY >= TITLE_HEIGHT + 1 && scaledY < TITLE_HEIGHT + 1 + DROPDOWN_HEIGHT) {
            if (placements.size() > 1) {
                dropdownOpen = !dropdownOpen;
                return true;
            }
        }

        if (dropdownOpen) {
            int dropdownListTop = TITLE_HEIGHT + 1 + DROPDOWN_HEIGHT;
            int dropdownListHeight = Math.min(placements.size() * ITEM_HEIGHT, this.height - TITLE_HEIGHT - FOOTER_HEIGHT - DROPDOWN_HEIGHT);

            if (scaledY >= dropdownListTop && scaledY < dropdownListTop + dropdownListHeight) {
                int clickedIndex = (int)((scaledY - dropdownListTop) / ITEM_HEIGHT);
                if (clickedIndex >= 0 && clickedIndex < placements.size()) {
                    selectedPlacementIndex = clickedIndex;
                    dropdownOpen = false;
                    loadMaterialsForCurrentPlacement();
                    this.width = calculateWidth();
                    return true;
                }
            }

            dropdownOpen = false;
            return true;
        }

        return false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
