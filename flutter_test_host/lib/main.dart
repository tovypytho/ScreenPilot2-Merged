import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

const MethodChannel _captureChannel =
    MethodChannel('id.eujian.cbt.screenpilot/capture');

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'ScreenPilot Capture Host',
      theme: ThemeData(
        primarySwatch: Colors.indigo,
      ),
      home: const CaptureHostPage(title: 'ScreenPilot Capture Host'),
    );
  }
}

class CaptureHostPage extends StatefulWidget {
  const CaptureHostPage({super.key, required this.title});

  final String title;

  @override
  State<CaptureHostPage> createState() => _CaptureHostPageState();
}

class _CaptureHostPageState extends State<CaptureHostPage> {
  String _imagePath = '';
  String _status = 'Idle';
  String _dimensions = '';
  bool _busy = false;

  Future<void> _capture() async {
    setState(() {
      _busy = true;
      _status = 'Capturing…';
    });
    try {
      final result =
          await _captureChannel.invokeMethod<Map<dynamic, dynamic>>('capture');
      final ok = result?['ok'] == true;
      setState(() {
        _busy = false;
        if (ok) {
          _imagePath = result!['path'] as String;
          _dimensions = '${result['width']} x ${result['height']}';
          _status = 'Success';
        } else {
          _imagePath = '';
          _dimensions = '';
          _status = 'Error: ${result?['error'] ?? 'unknown'}';
        }
      });
    } catch (e) {
      setState(() {
        _busy = false;
        _status = 'Exception: $e';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final hasImage = _imagePath.isNotEmpty;
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              _status,
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 12),
            if (_busy)
              const CircularProgressIndicator()
            else
              FilledButton.icon(
                onPressed: _capture,
                icon: const Icon(Icons.camera_alt),
                label: const Text('Capture via Bridge'),
              ),
            if (hasImage) ...[
              const SizedBox(height: 16),
              Image.file(
                File(_imagePath),
                width: 320,
                fit: BoxFit.contain,
                errorBuilder: (context, error, stackTrace) => Text(
                  'Failed to load image: $error',
                  textAlign: TextAlign.center,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Dimensions: $_dimensions',
                style: Theme.of(context).textTheme.bodyMedium,
              ),
            ],
          ],
        ),
      ),
    );
  }
}
