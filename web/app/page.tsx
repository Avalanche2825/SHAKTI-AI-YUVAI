'use client'

import { Shield, Brain, Camera, MapPin, Radio, Scale, DollarSign, Calculator, Download, Star, CheckCircle, AlertTriangle, Github, Mail } from 'lucide-react'

export default function Home() {
  return (
    <main className="min-h-screen bg-gradient-to-b from-pink-50 to-purple-50">
      {/* Navigation */}
      <nav className="fixed top-0 w-full bg-white/80 backdrop-blur-md shadow-sm z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center space-x-2">
              <Shield className="w-8 h-8 text-pink-600" />
              <span className="text-2xl font-bold bg-gradient-to-r from-pink-600 to-purple-600 bg-clip-text text-transparent">
                SHAKTI AI
              </span>
            </div>
            <div className="hidden md:flex space-x-8">
              <a href="#features" className="text-gray-700 hover:text-pink-600 transition">Features</a>
              <a href="#how-it-works" className="text-gray-700 hover:text-pink-600 transition">How It Works</a>
              <a href="#download" className="text-gray-700 hover:text-pink-600 transition">Download</a>
              <a href="#about" className="text-gray-700 hover:text-pink-600 transition">About</a>
            </div>
            <a 
              href="#download" 
              className="bg-gradient-to-r from-pink-600 to-purple-600 text-white px-6 py-2 rounded-full hover:shadow-lg transition"
            >
              Download App
            </a>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="pt-32 pb-20 px-4">
        <div className="max-w-7xl mx-auto text-center">
          <div className="animate-fadeIn">
            <div className="inline-flex items-center space-x-2 bg-pink-100 px-4 py-2 rounded-full mb-6">
              <Star className="w-4 h-4 text-pink-600" />
              <span className="text-pink-600 font-semibold">India's First AI-Powered Women's Safety App</span>
            </div>
            <h1 className="text-5xl md:text-7xl font-bold mb-6 bg-gradient-to-r from-pink-600 via-purple-600 to-pink-600 bg-clip-text text-transparent">
              Your Safety Guardian,<br />Powered by AI
            </h1>
            <p className="text-xl md:text-2xl text-gray-600 mb-8 max-w-3xl mx-auto">
              Automatic threat detection through advanced ML. No manual activation needed. 
              Disguised as a calculator for your protection.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <a 
                href="#download" 
                className="bg-gradient-to-r from-pink-600 to-purple-600 text-white px-8 py-4 rounded-full text-lg font-semibold hover:shadow-2xl transform hover:scale-105 transition inline-flex items-center justify-center space-x-2"
              >
                <Download className="w-5 h-5" />
                <span>Download APK (42 MB)</span>
              </a>
              <a 
                href="#how-it-works" 
                className="bg-white text-pink-600 px-8 py-4 rounded-full text-lg font-semibold hover:shadow-lg transition border-2 border-pink-600"
              >
                Learn How It Works
              </a>
            </div>
            <div className="mt-8 flex flex-wrap justify-center gap-6 text-sm text-gray-600">
              <div className="flex items-center space-x-2">
                <CheckCircle className="w-5 h-5 text-green-600" />
                <span>100% Free & Open Source</span>
              </div>
              <div className="flex items-center space-x-2">
                <CheckCircle className="w-5 h-5 text-green-600" />
                <span>Works Offline</span>
              </div>
              <div className="flex items-center space-x-2">
                <CheckCircle className="w-5 h-5 text-green-600" />
                <span>No Data Collection</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8 text-center">
            <div>
              <div className="text-4xl font-bold text-pink-600 mb-2">521</div>
              <div className="text-gray-600">Audio Classes</div>
            </div>
            <div>
              <div className="text-4xl font-bold text-purple-600 mb-2">2</div>
              <div className="text-gray-600">Cameras Recording</div>
            </div>
            <div>
              <div className="text-4xl font-bold text-pink-600 mb-2">1km</div>
              <div className="text-gray-600">Alert Radius</div>
            </div>
            <div>
              <div className="text-4xl font-bold text-purple-600 mb-2">16</div>
              <div className="text-gray-600">IPC Sections</div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20 px-4">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-bold mb-4">Powerful Features</h2>
            <p className="text-xl text-gray-600">Everything you need for complete protection</p>
          </div>
          
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            {/* Feature 1 */}
            <div className="bg-white p-8 rounded-2xl shadow-lg hover:shadow-2xl transition">
              <div className="w-12 h-12 bg-pink-100 rounded-full flex items-center justify-center mb-4">
                <Brain className="w-6 h-6 text-pink-600" />
              </div>
              <h3 className="text-2xl font-bold mb-3">AI Threat Detection</h3>
              <p className="text-gray-600 mb-4">
                YAMNet ML model analyzes audio in real-time to detect screams, yells, crying, and gasps automatically.
              </p>
              <ul className="space-y-2 text-sm text-gray-600">
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>50-100ms latency</span>
                </li>
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>No manual activation</span>
                </li>
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>3.94 MB TensorFlow model</span>
                </li>
              </ul>
            </div>

            {/* Feature 2 */}
            <div className="bg-white p-8 rounded-2xl shadow-lg hover:shadow-2xl transition">
              <div className="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center mb-4">
                <Camera className="w-6 h-6 text-purple-600" />
              </div>
              <h3 className="text-2xl font-bold mb-3">Dual-Camera Recording</h3>
              <p className="text-gray-600 mb-4">
                Records from both front and back cameras simultaneously to capture attacker's face and surroundings.
              </p>
              <ul className="space-y-2 text-sm text-gray-600">
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>HD 720p video quality</span>
                </li>
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>Legal-grade evidence</span>
                </li>
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>Automatic timestamping</span>
                </li>
              </ul>
            </div>

            {/* Feature 3 */}
            <div className="bg-white p-8 rounded-2xl shadow-lg hover:shadow-2xl transition">
              <div className="w-12 h-12 bg-pink-100 rounded-full flex items-center justify-center mb-4">
                <MapPin className="w-6 h-6 text-pink-600" />
              </div>
              <h3 className="text-2xl font-bold mb-3">GPS Location Tracking</h3>
              <p className="text-gray-600 mb-4">
                High-accuracy GPS tracking with reverse geocoding to convert coordinates into readable addresses.
              </p>
              <ul className="space-y-2 text-sm text-gray-600">
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>5-second updates</span>
                </li>
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>Location history</span>
                </li>
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>Geocoded addresses</span>
                </li>
              </ul>
            </div>

            {/* Feature 4 */}
            <div className="bg-white p-8 rounded-2xl shadow-lg hover:shadow-2xl transition">
              <div className="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center mb-4">
                <Radio className="w-6 h-6 text-purple-600" />
              </div>
              <h3 className="text-2xl font-bold mb-3">Offline Community Network</h3>
              <p className="text-gray-600 mb-4">
                BLE mesh network alerts nearby SHAKTI users within 1km radius, even without internet.
              </p>
              <ul className="space-y-2 text-sm text-gray-600">
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>Works offline</span>
                </li>
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>1km coverage radius</span>
                </li>
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>Distance calculation</span>
                </li>
              </ul>
            </div>

            {/* Feature 5 */}
            <div className="bg-white p-8 rounded-2xl shadow-lg hover:shadow-2xl transition">
              <div className="w-12 h-12 bg-pink-100 rounded-full flex items-center justify-center mb-4">
                <Scale className="w-6 h-6 text-pink-600" />
              </div>
              <h3 className="text-2xl font-bold mb-3">NYAY Legal AI</h3>
              <p className="text-gray-600 mb-4">
                Auto-generates FIR with correct IPC sections, calculates case strength, and recommends lawyers.
              </p>
              <ul className="space-y-2 text-sm text-gray-600">
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>16 IPC sections covered</span>
                </li>
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>Case strength calculator</span>
                </li>
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>Lawyer recommendations</span>
                </li>
              </ul>
            </div>

            {/* Feature 6 */}
            <div className="bg-white p-8 rounded-2xl shadow-lg hover:shadow-2xl transition">
              <div className="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center mb-4">
                <DollarSign className="w-6 h-6 text-purple-600" />
              </div>
              <h3 className="text-2xl font-bold mb-3">Escape Planner</h3>
              <p className="text-gray-600 mb-4">
                Financial calculator for escape planning with safe house finder and funding sources.
              </p>
              <ul className="space-y-2 text-sm text-gray-600">
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>₹90K-2.5L budgets</span>
                </li>
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>Safe house database</span>
                </li>
                <li className="flex items-center space-x-2">
                  <CheckCircle className="w-4 h-4 text-green-600" />
                  <span>7-step timeline</span>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section id="how-it-works" className="py-20 px-4 bg-white">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl md:text-5xl font-bold mb-4">How It Works</h2>
            <p className="text-xl text-gray-600">Simple, automatic, and effective</p>
          </div>

          <div className="grid md:grid-cols-4 gap-8">
            <div className="text-center">
              <div className="w-16 h-16 bg-gradient-to-r from-pink-600 to-purple-600 text-white rounded-full flex items-center justify-center text-2xl font-bold mx-auto mb-4">1</div>
              <h3 className="text-xl font-bold mb-2">Download & Install</h3>
              <p className="text-gray-600">Install the app and complete onboarding. Grant all permissions for full protection.</p>
            </div>
            <div className="text-center">
              <div className="w-16 h-16 bg-gradient-to-r from-pink-600 to-purple-600 text-white rounded-full flex items-center justify-center text-2xl font-bold mx-auto mb-4">2</div>
              <h3 className="text-xl font-bold mb-2">Enable Monitoring</h3>
              <p className="text-gray-600">Long-press AC button on calculator. Green dot means you're protected 24/7.</p>
            </div>
            <div className="text-center">
              <div className="w-16 h-16 bg-gradient-to-r from-pink-600 to-purple-600 text-white rounded-full flex items-center justify-center text-2xl font-bold mx-auto mb-4">3</div>
              <h3 className="text-xl font-bold mb-2">Automatic Detection</h3>
              <p className="text-gray-600">AI detects threats through audio. No manual activation needed in emergencies.</p>
            </div>
            <div className="text-center">
              <div className="w-16 h-16 bg-gradient-to-r from-pink-600 to-purple-600 text-white rounded-full flex items-center justify-center text-2xl font-bold mx-auto mb-4">4</div>
              <h3 className="text-xl font-bold mb-2">Evidence & Alerts</h3>
              <p className="text-gray-600">Captures video, GPS, and alerts nearby users. Generate FIR with legal AI.</p>
            </div>
          </div>

          <div className="mt-16 bg-gradient-to-r from-pink-50 to-purple-50 p-8 rounded-2xl">
            <div className="flex items-start space-x-4">
              <Calculator className="w-8 h-8 text-pink-600 mt-1" />
              <div>
                <h3 className="text-2xl font-bold mb-3">Secret Calculator Disguise</h3>
                <p className="text-gray-600 mb-4">
                  The app appears as a fully functional calculator to protect you from abusers. Use secret codes to access features:
                </p>
                <div className="grid sm:grid-cols-3 gap-4">
                  <div className="bg-white p-4 rounded-lg">
                    <code className="text-pink-600 font-bold text-lg">999=</code>
                    <p className="text-sm text-gray-600 mt-1">Open Dashboard</p>
                  </div>
                  <div className="bg-white p-4 rounded-lg">
                    <code className="text-pink-600 font-bold text-lg">911=</code>
                    <p className="text-sm text-gray-600 mt-1">Emergency SOS</p>
                  </div>
                  <div className="bg-white p-4 rounded-lg">
                    <code className="text-pink-600 font-bold text-lg">777=</code>
                    <p className="text-sm text-gray-600 mt-1">Open Settings</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Download Section */}
      <section id="download" className="py-20 px-4 bg-gradient-to-r from-pink-600 to-purple-600 text-white">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-4xl md:text-5xl font-bold mb-6">Download SHAKTI AI</h2>
          <p className="text-xl mb-8 opacity-90">
            Android 7.0+ • 42 MB • Free & Open Source
          </p>
          
          <div className="bg-white/10 backdrop-blur-md p-8 rounded-2xl mb-8">
            <h3 className="text-2xl font-bold mb-4">Installation Steps</h3>
            <ol className="text-left space-y-4 max-w-2xl mx-auto">
              <li className="flex items-start space-x-3">
                <span className="bg-white text-pink-600 w-8 h-8 rounded-full flex items-center justify-center font-bold flex-shrink-0">1</span>
                <span>Download the APK file (42.11 MB)</span>
              </li>
              <li className="flex items-start space-x-3">
                <span className="bg-white text-pink-600 w-8 h-8 rounded-full flex items-center justify-center font-bold flex-shrink-0">2</span>
                <span>Enable "Install from Unknown Sources" in Settings → Security</span>
              </li>
              <li className="flex items-start space-x-3">
                <span className="bg-white text-pink-600 w-8 h-8 rounded-full flex items-center justify-center font-bold flex-shrink-0">3</span>
                <span>Open the APK file and tap Install</span>
              </li>
              <li className="flex items-start space-x-3">
                <span className="bg-white text-pink-600 w-8 h-8 rounded-full flex items-center justify-center font-bold flex-shrink-0">4</span>
                <span>Complete onboarding and grant all permissions</span>
              </li>
            </ol>
          </div>

          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <a 
              href="https://github.com/Avalanche2825/SHAKTI-AI-YUVAI/releases/latest/download/app-release.apk" 
              className="bg-white text-pink-600 px-8 py-4 rounded-full text-lg font-semibold hover:shadow-2xl transform hover:scale-105 transition inline-flex items-center justify-center space-x-2"
            >
              <Download className="w-5 h-5" />
              <span>Download APK</span>
            </a>
            <a 
              href="https://github.com/Avalanche2825/SHAKTI-AI-YUVAI" 
              target="_blank"
              rel="noopener noreferrer"
              className="bg-white/20 backdrop-blur-md text-white px-8 py-4 rounded-full text-lg font-semibold hover:bg-white/30 transition inline-flex items-center justify-center space-x-2"
            >
              <Github className="w-5 h-5" />
              <span>View on GitHub</span>
            </a>
          </div>

          <div className="mt-8 flex items-center justify-center space-x-2">
            <AlertTriangle className="w-5 h-5" />
            <p className="text-sm opacity-90">
              Currently unsigned APK. Safe to install for testing. Production version will be signed.
            </p>
          </div>
        </div>
      </section>

      {/* About Section */}
      <section id="about" className="py-20 px-4 bg-white">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-12">
            <h2 className="text-4xl md:text-5xl font-bold mb-4">Built for Indian Women</h2>
            <p className="text-xl text-gray-600">Addressing real safety challenges with advanced technology</p>
          </div>

          <div className="grid md:grid-cols-3 gap-8 mb-12">
            <div className="text-center">
              <div className="text-5xl font-bold text-pink-600 mb-2">16</div>
              <div className="text-gray-600">IPC Sections Covered</div>
              <div className="text-sm text-gray-500 mt-2">498A, 354, 375/376, 503/504, 67 IT Act, etc.</div>
            </div>
            <div className="text-center">
              <div className="text-5xl font-bold text-purple-600 mb-2">5</div>
              <div className="text-gray-600">Languages Supported</div>
              <div className="text-sm text-gray-500 mt-2">English, Hindi, Bengali, Kannada, Tamil</div>
            </div>
            <div className="text-center">
              <div className="text-5xl font-bold text-pink-600 mb-2">100%</div>
              <div className="text-gray-600">Privacy Protected</div>
              <div className="text-sm text-gray-500 mt-2">All data stays on your device</div>
            </div>
          </div>

          <div className="bg-gradient-to-r from-pink-50 to-purple-50 p-8 rounded-2xl">
            <h3 className="text-2xl font-bold mb-4 text-center">Open Source & Transparent</h3>
            <p className="text-gray-600 text-center mb-6 max-w-3xl mx-auto">
              SHAKTI AI is completely open source. Our code is auditable by security researchers and the community. 
              We believe in transparency when it comes to women's safety.
            </p>
            <div className="flex flex-wrap justify-center gap-4">
              <a 
                href="https://github.com/Avalanche2825/SHAKTI-AI-YUVAI" 
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center space-x-2 bg-white px-6 py-3 rounded-full hover:shadow-lg transition"
              >
                <Github className="w-5 h-5" />
                <span>View Source Code</span>
              </a>
              <a 
                href="mailto:abhyudayjain59@gmail.com" 
                className="flex items-center space-x-2 bg-white px-6 py-3 rounded-full hover:shadow-lg transition"
              >
                <Mail className="w-5 h-5" />
                <span>Contact Developer</span>
              </a>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-white py-12 px-4">
        <div className="max-w-7xl mx-auto">
          <div className="grid md:grid-cols-4 gap-8 mb-8">
            <div>
              <div className="flex items-center space-x-2 mb-4">
                <Shield className="w-6 h-6 text-pink-500" />
                <span className="text-xl font-bold">SHAKTI AI</span>
              </div>
              <p className="text-gray-400 text-sm">
                India's first AI-powered women's safety app with automatic threat detection.
              </p>
            </div>
            <div>
              <h4 className="font-bold mb-4">Quick Links</h4>
              <ul className="space-y-2 text-gray-400 text-sm">
                <li><a href="#features" className="hover:text-white transition">Features</a></li>
                <li><a href="#how-it-works" className="hover:text-white transition">How It Works</a></li>
                <li><a href="#download" className="hover:text-white transition">Download</a></li>
                <li><a href="#about" className="hover:text-white transition">About</a></li>
              </ul>
            </div>
            <div>
              <h4 className="font-bold mb-4">Resources</h4>
              <ul className="space-y-2 text-gray-400 text-sm">
                <li><a href="https://github.com/Avalanche2825/SHAKTI-AI-YUVAI" target="_blank" rel="noopener noreferrer" className="hover:text-white transition">GitHub Repository</a></li>
                <li><a href="https://github.com/Avalanche2825/SHAKTI-AI-YUVAI/blob/main/README.md" target="_blank" rel="noopener noreferrer" className="hover:text-white transition">Documentation</a></li>
                <li><a href="https://github.com/Avalanche2825/SHAKTI-AI-YUVAI/blob/main/DEPLOYMENT_GUIDE.md" target="_blank" rel="noopener noreferrer" className="hover:text-white transition">Deployment Guide</a></li>
                <li><a href="https://github.com/Avalanche2825/SHAKTI-AI-YUVAI/issues" target="_blank" rel="noopener noreferrer" className="hover:text-white transition">Report Issues</a></li>
              </ul>
            </div>
            <div>
              <h4 className="font-bold mb-4">Emergency Contacts</h4>
              <ul className="space-y-2 text-gray-400 text-sm">
                <li>Police: <span className="text-white font-bold">100</span></li>
                <li>Women's Helpline: <span className="text-white font-bold">181</span></li>
                <li>Domestic Violence: <span className="text-white font-bold">181</span></li>
                <li>Child Helpline: <span className="text-white font-bold">1098</span></li>
              </ul>
            </div>
          </div>
          <div className="border-t border-gray-800 pt-8 text-center text-gray-400 text-sm">
            <p>&copy; 2025 SHAKTI AI. Built with ❤️ for the safety of Indian women.</p>
            <p className="mt-2">Developer: <a href="https://github.com/Avalanche2825" target="_blank" rel="noopener noreferrer" className="text-pink-500 hover:text-pink-400 transition">Abhyuday Jain</a></p>
          </div>
        </div>
      </footer>
    </main>
  )
}
