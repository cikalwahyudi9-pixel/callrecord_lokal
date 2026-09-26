import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:shared_preferences/shared_preferences.dart';

const _channel = MethodChannel('call_recorder_local/native');

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const CallRecorderApp());
}

class CallRecorderApp extends StatelessWidget {
  const CallRecorderApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Local Call Recorder',
      theme: ThemeData(useMaterial3: true, colorSchemeSeed: Colors.indigo),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});
  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  bool enabled = false;
  bool configured = false;
  String status = 'Belum disiapkan';
  String folder = 'Penyimpanan aplikasi';

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final prefs = await SharedPreferences.getInstance();
    final configuredValue = prefs.getBool('configured') ?? false;
    final enabledValue = prefs.getBool('enabled') ?? false;
    setState(() {
      configured = configuredValue;
      enabled = enabledValue;
      status = enabled ? 'Aktif' : 'Nonaktif';
    });
    try {
      folder = await _channel.invokeMethod<String>('recordingDirectory') ?? folder;
      setState(() {});
    } catch (_) {}
  }

  Future<void> _setup() async {
    final mic = await Permission.microphone.request();
    final phone = await Permission.phone.request();
    final notifications = await Permission.notification.request();

    if (!mic.isGranted || !phone.isGranted) {
      setState(() => status = 'Permission microphone/telepon belum diberikan');
      return;
    }

    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('configured', true);
    await prefs.setBool('enabled', true);

    try {
      await _channel.invokeMethod('enableAutomation');
    } catch (_) {}

    setState(() {
      configured = true;
      enabled = true;
      status = notifications.isGranted ? 'Aktif' : 'Aktif (notifikasi belum diizinkan)';
    });
  }

  Future<void> _toggle(bool value) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('enabled', value);
    try {
      await _channel.invokeMethod(value ? 'enableAutomation' : 'disableAutomation');
    } catch (_) {}
    setState(() {
      enabled = value;
      status = value ? 'Aktif' : 'Nonaktif';
    });
  }

  Future<void> _testRecording() async {
    try {
      await _channel.invokeMethod('testRecording');
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Tes rekaman dimulai selama 5 detik.')));
    } on PlatformException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message ?? 'Tes gagal')));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Local Call Recorder')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text(status, style: Theme.of(context).textTheme.headlineSmall),
                const SizedBox(height: 8),
                const Text('Rekaman hanya disimpan di perangkat ini.'),
              ]),
            ),
          ),
          const SizedBox(height: 16),
          if (!configured)
            FilledButton.icon(
              onPressed: _setup,
              icon: const Icon(Icons.settings),
              label: const Text('Setup sekali'),
            ),
          if (configured) ...[
            SwitchListTile.adaptive(
              title: const Text('Otomatis aktif'),
              subtitle: const Text('Bekerja ketika layar aktif; layar mati menghentikan perekaman baru.'),
              value: enabled,
              onChanged: _toggle,
            ),
            ListTile(
              leading: const Icon(Icons.folder),
              title: const Text('Folder rekaman'),
              subtitle: Text(folder),
            ),
            const SizedBox(height: 8),
            OutlinedButton.icon(
              onPressed: _testRecording,
              icon: const Icon(Icons.mic),
              label: const Text('Tes microphone'),
            ),
          ],
          const SizedBox(height: 24),
          const Text(
            'Catatan Android: akses audio panggilan dua arah tidak dijamin pada semua perangkat. Aplikasi ini tidak menggunakan Accessibility Service untuk mengakali pembatasan tersebut.',
          ),
        ],
      ),
    );
  }
}
