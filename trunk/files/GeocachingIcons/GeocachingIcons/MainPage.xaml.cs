using System.Windows;
using System.Windows.Controls;

namespace GeocachingIcons
{
	public partial class MainPage : UserControl
	{
		public MainPage()
		{
			// Required to initialize variables
			InitializeComponent();
		}

        private void SaveButtonClick(object sender, RoutedEventArgs e)
        {
            this.Canvas.SaveImage();
        }
	}
}