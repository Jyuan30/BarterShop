package org.xymy.barterShop;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ServerListener implements Listener {
    private final BarterShop plugin;

    public ServerListener(BarterShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        this.plugin.getLogger().info("正在延迟加载商店数据...");
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // 尝试初始化数据
                    plugin.getShopManager().initialize();

                    // 如果运行到这里没有报错，说明成功了
                    plugin.getLogger().info("商店数据初始化成功！");

                    // 成功后必须取消这个定时任务，否则会一直循环下去
                    this.cancel();

                } catch (Throwable t) {
                    // 捕捉到任何错误（包括 NoClassDefFoundError 等）
                    plugin.getLogger().severe("商店数据初始化时发生错误！5秒后将自动重试...");

                    // 打印错误堆栈，方便你在后台查看具体原因
                    // t.printStackTrace();

                    // 这里不需要写额外代码，因为定时任务会根据设定的间隔自动再次执行 run()
                }
            }
        }.runTaskTimerAsynchronously(this.plugin, 100L, 100L);
    }
}
