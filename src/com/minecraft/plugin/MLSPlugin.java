package com.minecraft.plugin;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

public class MLSPlugin extends JavaPlugin implements Listener {
    private static int _price;
    private static FileConfiguration _playerData;

    public MLSPlugin() {}

    @Override
    public void onLoad() {
        this.saveDefaultConfig();
        this.saveResource("playerData.yml", false);
        _playerData = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "playerData.yml"));
        _price = getConfig().getInt("Price");
    }

    @Override
    public void onEnable() {
        try {
            getCommand("mls").setExecutor(this);
        }
        catch(NullPointerException e) {
            e.printStackTrace();
        }
        //Bukkit.getPluginManager().registerEvents(this,this);
    }

    @Override
    public void onDisable() {
    }

    //@EventHandler
    //public void onPlayerJoin(PlayerJoinEvent event) {
    //    //添加新玩家数据
    //    String dateKey = event.getPlayer().getName() + ".LastDate";
    //    String dayOfWeekKey = event.getPlayer().getName() + ".LastDayOfWeek";
    //    if(!_playerData.contains(dateKey)) {
    //        _playerData.set(dateKey, true);
    //        _playerData.set(dayOfWeekKey, true);
    //        _SavePlayerData();
    //    }
    //}

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player recipient;
        //无参数
        if(strings.length == 0) {
            if(commandSender instanceof Player) {
                recipient = (Player)commandSender;
            }
            //非玩家
            else {
                commandSender.sendMessage("只有玩家可以领取低保。");
                return false;
            }
        }
        //一个参数：玩家名
        else if(strings.length == 1) {
            recipient = Bukkit.getPlayerExact(strings[0]);
            if(!recipient.isOnline()) {
                commandSender.sendMessage(ChatColor.RED + "[MLS]: 该玩家不存在或不在线。");
                return false;
            }
        }
        //参数过多
        else {
            commandSender.sendMessage(ChatColor.RED + "usage: /mls <player>");
            return false;
        }

        try {
            //穷
            if(Economy.getMoneyExact(recipient.getName()).intValueExact() < _price) {
                ////在领取日
                //if(GetDayOfWeek() == _day) {
                //    try {
                //        Economy.add(recipient.getName(), BigDecimal.valueOf(_price));
                //        recipient.sendMessage(ChatColor.GREEN + "[MLS]: 你领取了低保。");
                //        _playerData.set(recipient.getName(), false);
                //        _playerData.set("Updated", false);
                //        _SavePlayerData();
                //    }
                //    catch(NoLoanPermittedException e) {
                //        e.printStackTrace();
                //    }
                //}
                ////不在领取日
                //else {
                //    String weekday;
                //    switch(_day) {
                //        case 1:
                //            weekday = "一";
                //            break;
                //        case 2:
                //            weekday = "二";
                //            break;
                //        case 3:
                //            weekday = "三";
                //            break;
                //        case 4:
                //            weekday = "四";
                //            break;
                //        case 5:
                //            weekday = "五";
                //            break;
                //        case 6:
                //            weekday = "六";
                //            break;
                //        case 7:
                //        default:
                //            weekday = "日";
                //            break;
                //    }
                //    recipient.sendMessage(ChatColor.RED + "[MLS]: 只有周" + weekday + "才能领取低保。");
                //}
                LocalDate today = LocalDate.now();
                String dateKey = recipient.getName() + ".Date";
                //未领取过，或上次领取时间非本周
                if(_playerData.get(recipient.getName()) == null ||
                        !daysInWeekCompare(LocalDate.of(
                                _playerData.getInt(dateKey + ".Year"),
                                _playerData.getInt(dateKey + ".Month"),
                                _playerData.getInt(dateKey + ".Day")
                        ), today)) {
                    try {
                        Economy.add(recipient.getName(), BigDecimal.valueOf(_price));
                    }
                    catch(NoLoanPermittedException e) {
                        e.printStackTrace();
                    }
                    recipient.sendMessage(ChatColor.GREEN + "[MLS]: 你领取了低保。");

                    _playerData.set(dateKey + ".Year", today.getYear());
                    _playerData.set(dateKey + ".Month", today.getMonthValue());
                    _playerData.set(dateKey + ".Day", today.getDayOfMonth());
                    try {
                        _playerData.save(new File(this.getDataFolder(), "playerData.yml"));
                    }
                    catch(java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
                //不在领取日
                else {
                    recipient.sendMessage(ChatColor.RED + "[MLS]: 你这周已经领取过低保了。");
                }
            }
            //钱包大于等于30
            else {
                recipient.sendMessage(ChatColor.RED + "[MLS]: 钱包不足" + ChatColor.WHITE + _price + ChatColor.RED + "时才能领取低保。");
            }
        }
        catch(UserDoesNotExistException e) {
            e.printStackTrace();
        }
        return false;
    }

    //获取今天的星期
    public int getDayOfWeek(LocalDate time) {
        switch(time.getDayOfWeek()) {
            case MONDAY:
                return 1;
            case TUESDAY:
                return 2;
            case WEDNESDAY:
                return 3;
            case THURSDAY:
                return 4;
            case FRIDAY:
                return 5;
            case SATURDAY:
                return 6;
            case SUNDAY:
            default:
                return 7;
        }
    }

    public int getDayOfWeek() {
        return getDayOfWeek(LocalDate.now());
    }

    //两天在同一周内，为真
    public boolean daysInWeekCompare(final LocalDate oldDate, final LocalDate newDate) {
        Period time = Period.between(oldDate, newDate);
        return time.getMonths() == 0 && time.getYears() == 0 && time.getDays() < 7;
    }
}
