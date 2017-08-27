local module = {}

local function ota_cmd(command, arguments)
    if "restart" == command then
        print("Restarting")
        node.restart()
    elseif "openfile" == command then
        print("Start writing file")
        file.remove("tmpfile")
        file.open("tmpfile", "w+")
    elseif "writeline" == command then
        print("Write Line: " .. arguments)
        file.writeline(arguments)
    elseif "closefile" == command then
        file.close()
        file.remove(arguments)
        file.rename("tmpfile", arguments)       
        print("Wrote " .. arguments)
    else
        print("Unknown command: " .. command)
    end
end

local function mqtt_start()  
    app.prepare()

    local m = mqtt.Client(config.ID, 120, config.USER, config.PASS)

    m:lwt(config.ENDPOINT .. "state", "dead", 0, 1)

    print("Connecting to broker...")
    m:connect(config.HOST, config.PORT, 0, function(con) 
        print("Connected")

        m:publish(config.ENDPOINT .. "state","online",0,1)
        m:publish(config.ENDPOINT .. "version",config.VERSION,0,1)

        local otacmdtopic = config.ENDPOINT .. "otacmd"
        m:subscribe(otacmdtopic, 2, function(conn)
            print("Successfully subscribed to " .. otacmdtopic)
        end)

        m:on("message", function(conn, topic, data) 
            if data ~= nil then
                if topic == otacmdtopic then
                    local sepPos = string.find(data, ":")
                    local command = string.sub(data, 1, sepPos - 1)
                    local arguments = string.sub(data, sepPos + 1)
                    ota_cmd(command, arguments)
                else
	                print("Received on topic: " .. topic)
                    app.on_message(topic, data)
                end
            end
        end)
        
        m:on("offline", function(conn)
        	print("MQTTT went offline, rebooting")
        	node.restart()
        end)
        
        app.register(m)        
    end, function(conn, reason)
    	print("Connection failed: " .. reason)
    	tmr.create():alarm(10 * 1000, tmr.ALARM_SINGLE, mqtt_start)
    end) 
end

local function wifi_wait_ip()  
  if wifi.sta.getip() == nil then
    print("IP unavailable, Waiting...")
  else
    tmr.stop(1)
    print("MAC address is: " .. wifi.ap.getmac())
    print("IP is " .. wifi.sta.getip())

    wifi.eventmon.register(wifi.eventmon.STA_DISCONNECTED, function(T)
      print("Wifi disconnected, rebooting: " .. T.reason)
      node.restart()
    end)

    mqtt_start()
  end
end

local function wifi_start()
  local cfg = {}
  cfg.ssid = config.SSID
  cfg.pwd = config.SSID_PASS
  wifi.setmode(wifi.STATION);
  wifi.sta.config(cfg)
  
  print("Connecting to " .. config.SSID .. " ...")
  tmr.alarm(1, 2500, 1, wifi_wait_ip)
end

function module.start()  
  wifi_start()
end

return module
