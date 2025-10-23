# Integración con n8n - Notificaciones de Emergencia

## 📋 Resumen de la Actualización

Se ha implementado exitosamente la integración con n8n para enviar notificaciones automáticas de emergencia cada vez que se guarda una nueva medición de presión arterial.

## 🆕 Nuevas Funcionalidades

### 1. Campos Agregados en Configuración
- **Tu número de teléfono**: Campo para ingresar el número del usuario
- **URL del webhook de n8n**: Campo para configurar la URL del webhook que procesará las notificaciones

### 2. Envío Automático de Alertas
Cuando el usuario guarda un registro de presión arterial, la aplicación:
- ✅ Guarda el registro en la base de datos
- ✅ Genera automáticamente un diagnóstico basado en los valores
- ✅ Envía un POST request a n8n con los datos
- ✅ Muestra una notificación de éxito o error

## 📊 Formato de Datos Enviados a n8n

La aplicación envía un JSON con la siguiente estructura:

```json
{
  "nombre_usuario": "Juan Pérez",
  "numero_usuario": "+502XXXXXXXX",
  "nombre_contacto_emergencia": "María López",
  "numero_contacto_emergencia": "+502YYYYYYYY",
  "presion_sistolica": 135,
  "presion_diastolica": 90,
  "diagnostico": "Hipertensión nivel 1"
}
```

## 🔧 Configuración Inicial

### Paso 1: Configurar el Usuario
1. Abre la aplicación
2. Ve a **Configuración/Ajustes**
3. Completa los campos:
   - Nombre
   - Edad
   - **Tu número de teléfono** (NUEVO)

### Paso 2: Configurar Contacto de Emergencia
En la misma pantalla de configuración:
1. Ingresa el **Nombre del contacto de emergencia**
2. Ingresa el **Teléfono del contacto**
3. Ingresa la **URL del webhook de n8n** (NUEVO)
   - Ejemplo: `https://tu-dominio.app.n8n.cloud/webhook/presion-arterial`

### Paso 3: Guardar Configuración
- Presiona el botón **"Guardar Configuración"**

## 🚀 Uso

Una vez configurado, simplemente:
1. Ve a **"Nuevo Registro"**
2. Ingresa los datos de presión arterial
3. Presiona **"Guardar"**
4. La app automáticamente:
   - Guardará el registro
   - Enviará la alerta a n8n
   - Mostrará "✅ Mensaje de emergencia enviado correctamente"

## 📝 Diagnósticos Posibles

La aplicación genera los siguientes diagnósticos automáticamente:

| Sistólica | Diastólica | Diagnóstico |
|-----------|------------|-------------|
| < 90 | < 60 | Presión baja |
| < 120 | < 80 | Presión normal |
| 120-129 | < 80 | Presión elevada |
| 130-139 | 80-89 | Hipertensión nivel 1 |
| 140-179 | 90-119 | Hipertensión nivel 2 |
| ≥ 180 | ≥ 120 | Crisis hipertensiva |

## ⚠️ Validaciones Implementadas

La aplicación valida que:
- ✅ El contacto de emergencia esté configurado
- ✅ La URL del webhook de n8n esté configurada
- ✅ Los datos sean válidos antes de enviar

Si falta alguna configuración, **NO se mostrará error al usuario**, pero tampoco se enviará la notificación.

## 🔍 Debugging

Para verificar que todo funciona:
1. Revisa los logs de Android (Logcat)
2. Busca el tag `EmergencyNotification`
3. Verás mensajes como:
   - "Enviando alerta de emergencia a: [URL]"
   - "Alerta enviada exitosamente"
   - O errores específicos si algo falla

## 📱 Ejemplo de Configuración de n8n

En n8n, crea un workflow con:
1. **Webhook Node** (Trigger)
   - Method: POST
   - Path: `/webhook/presion-arterial`
   
2. **Function Node** (Procesar datos)
   - Extrae los datos del JSON recibido
   
3. **Send Message Node** (Enviar mensaje)
   - Telegram / WhatsApp / SMS
   - Mensaje ejemplo:
     ```
     ⚠️ ALERTA DE PRESIÓN ARTERIAL
     
     Usuario: {{$json.nombre_usuario}}
     Presión: {{$json.presion_sistolica}}/{{$json.presion_diastolica}} mmHg
     Diagnóstico: {{$json.diagnostico}}
     
     Contactar a: {{$json.nombre_usuario}} al {{$json.numero_usuario}}
     ```

## 🛠️ Archivos Modificados

### Nuevos Archivos:
- `EmergencyNotificationService.java` - Servicio para enviar notificaciones
- `N8nRequest.java` - Modelo de datos para el request
- `N8nApiService.java` - Interfaz Retrofit para n8n

### Archivos Actualizados:
- `UserSettings.java` - Agregados campos `userPhoneNumber` y `n8nWebhookUrl`
- `NewRecordFragment.java` - Integrado envío automático al guardar
- `SettingsFragment.java` - Agregados campos en UI
- `fragment_settings.xml` - Agregados inputs para nuevos campos
- `AppDatabase.java` - Versión actualizada a 2 con migración

## 🔐 Seguridad

- ⚠️ La URL del webhook NO está hardcodeada, es configurable por el usuario
- ⚠️ Los datos se envían por HTTPS (asegúrate de usar una URL segura)
- ⚠️ No se almacenan contraseñas ni tokens de autenticación

## 📞 Soporte

Si tienes problemas:
1. Verifica que la URL del webhook sea correcta
2. Prueba el webhook directamente con Postman
3. Revisa los logs de n8n para ver si recibe los datos
4. Verifica que el dispositivo tenga conexión a internet

---

**Nota**: Esta funcionalidad se ejecuta SIEMPRE que se guarda un registro, independientemente de si la presión es normal, alta o baja.

