--[[
	Performs the MQTT variable setup.
]]





-- MQTT client ID
local client_id = "Nico"
-- MQTT topic for measurements (temperature, humidity etc.)
local topic1 = "temperature"
-- MQTT topic for the thermal images
local topic2 = "thermal"

-- MQTT broker connection type
local broker_con_type = "tcp"
-- MQTT broker IP
local broker_url = "192.168.43.4"
-- MQTT broker port
local broker_port = 1883
-- Quality of Service for all MQTT messages
local qos = 0

-- Set default connection options
-- Have been hardcoded in Java because they can crash the entire backend if configured incorrectly - remember: keep it simple
--mqtt.connOpts:setCleanSession(true)





-- SETTER
if not mqtt.firstRun then
	mqtt:reset() -- Reset MQTT connections and client first
end

mqtt:setClientId(client_id)
mqtt:setClientBroker(broker_con_type .."://"..broker_url..":"..broker_port) -- Assemble broker address here and then pass full address to Java
mqtt:setQos(qos)
mqtt:setTopic1(topic1)
mqtt:setTopic2(topic2)

mqtt.firstRun = false
