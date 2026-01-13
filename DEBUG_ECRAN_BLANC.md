# 🐛 Debug : Écran Blanc dans PaymentActivity

## 🔍 Problème

Quand vous cliquez sur "Payer", vous obtenez un **écran blanc** au lieu de la page de paiement.

## ✅ Solutions Implémentées

### 1. Changement de méthode de chargement
- **Avant** : `loadDataWithBaseURL(null, ...)` → Peut causer un écran blanc
- **Maintenant** : Base64 en premier, puis fallbacks multiples

### 2. Logs détaillés
- Vérification de la génération du HTML
- Vérification du chargement dans la WebView
- Vérification de l'affichage de la page

## 🧪 Comment Diagnostiquer

### Étape 1 : Vérifier les logs Android

Dans **Logcat** (filtre : `PaymentActivity`), cherchez ces messages :

#### ✅ Si le HTML est généré :
```
📋 Données pour la page de paiement:
   Montant: 15.0
   UserId: ...
✅ HTML généré avec succès, longueur: XXXX caractères
```

#### ✅ Si le HTML est chargé :
```
📄 Longueur du HTML: XXXX caractères
🔄 Chargement du HTML via Base64...
✅ HTML chargé via Base64
```

#### ✅ Si la page est affichée :
```
✅ Page finished: data:text/html;charset=utf-8;base64,...
🔍 Vérification de la page chargée...
   Body existe: true
   Body innerHTML length: XXXX
   Body visible: true
```

### Étape 2 : Vérifier les erreurs

Cherchez les messages d'erreur :

```
❌ Le HTML généré est vide ou null!
❌ Erreur lors du chargement du HTML via Base64
❌ Body existe: false
❌ Lien Payer NON trouvé dans le DOM
```

## 🔧 Solutions selon les Erreurs

### Erreur 1 : "HTML généré est vide"
**Cause** : La méthode `buildPaymentUrl()` retourne null ou vide

**Solution** :
1. Vérifiez que `amount > 0`
2. Vérifiez que les données utilisateur sont présentes
3. Ajoutez des logs dans `buildPaymentUrl()`

### Erreur 2 : "Erreur lors du chargement du HTML"
**Cause** : Problème d'encodage ou de taille

**Solution** :
- Le code essaie automatiquement 3 méthodes :
  1. Base64 (première tentative)
  2. `loadDataWithBaseURL` avec base URL valide
  3. Encodage URL simple

### Erreur 3 : "Body existe: false"
**Cause** : La page ne s'est pas chargée correctement

**Solution** :
1. Vérifiez que JavaScript est activé : `webSettings.setJavaScriptEnabled(true)`
2. Vérifiez que la WebView est visible dans le layout
3. Vérifiez les permissions Internet dans le manifest

### Erreur 4 : "Lien Payer NON trouvé"
**Cause** : Le HTML n'est pas correctement généré ou chargé

**Solution** :
1. Vérifiez que le HTML contient bien `<a id="payButton">`
2. Vérifiez que le CSS ne cache pas les éléments (`display: none`)

## 🎯 Test Rapide

### Test 1 : Vérifier que la WebView est visible

Ajoutez temporairement ce code dans `onCreate()` :

```java
webView.setBackgroundColor(Color.WHITE); // Pour voir si la WebView est là
webView.setVisibility(View.VISIBLE);
```

### Test 2 : Charger une page HTML simple

Remplacez temporairement `loadPaymentPageFallback()` par :

```java
String testHtml = "<html><body><h1>Test</h1><p>Si vous voyez ceci, la WebView fonctionne!</p></body></html>";
loadHtmlInWebView(testHtml);
```

Si vous voyez "Test", alors le problème vient de la génération du HTML de paiement.

### Test 3 : Vérifier le layout XML

Vérifiez que `activity_payment.xml` contient bien :

```xml
<WebView
    android:id="@+id/webViewPayment"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

## 📊 Checklist de Vérification

- [ ] La WebView est déclarée dans le layout XML
- [ ] La WebView a `android:layout_width` et `android:layout_height` définis
- [ ] JavaScript est activé : `webSettings.setJavaScriptEnabled(true)`
- [ ] Permission Internet est dans le manifest
- [ ] `loadPaymentPageFallback()` est appelé
- [ ] Le HTML est généré (vérifier les logs)
- [ ] Le HTML est chargé (vérifier les logs)
- [ ] La page est affichée (vérifier les logs JavaScript)

## 🆘 Solution d'Urgence

Si RIEN ne fonctionne, utilisez cette méthode de chargement simplifiée :

```java
private void loadHtmlInWebView(String html) {
    // Méthode la plus simple et fiable
    String base64 = android.util.Base64.encodeToString(
        html.getBytes("UTF-8"), 
        android.util.Base64.NO_WRAP
    );
    String dataUrl = "data:text/html;charset=utf-8;base64," + base64;
    
    Log.d(TAG, "🔄 Chargement via: " + dataUrl.substring(0, Math.min(100, dataUrl.length())) + "...");
    webView.loadUrl(dataUrl);
    
    // Vérifier après 2 secondes
    webView.postDelayed(() -> {
        webView.evaluateJavascript("document.body ? 'OK' : 'ERREUR'", (result) -> {
            Log.d(TAG, "📊 Résultat: " + result);
            if ("ERREUR".equals(result)) {
                Toast.makeText(this, "Erreur: Page non chargée", Toast.LENGTH_LONG).show();
            }
        });
    }, 2000);
}
```

## 📝 Logs à Partager

Si le problème persiste, partagez ces logs :

1. Tous les logs avec le tag `PaymentActivity`
2. Les logs JavaScript de la console (si accessible)
3. Les erreurs dans Logcat (filtre : `Error`)

---

**Note** : Avec les changements récents, le HTML devrait maintenant s'afficher correctement. Si l'écran est toujours blanc, les logs vous diront exactement où est le problème.



